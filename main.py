from fastapi import APIRouter, FastAPI, File, Form, UploadFile
from fastapi.responses import JSONResponse
import base64
import json
import os
import sqlite3
import time
import random
import re
from datetime import datetime

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "data")
AUDIO_DIR = os.path.join(DATA_DIR, "audio")
DB_PATH = os.path.join(DATA_DIR, "voiceaid.db")

CAPTCHA_TTL_SECONDS = int(os.getenv("CAPTCHA_TTL_SECONDS", "300"))
SMS_ENABLED = os.getenv("SMS_ENABLED", "true").lower() in ("1", "true", "yes")
SMS_DEBUG_RETURN_CODE = os.getenv("SMS_DEBUG_RETURN_CODE", "false").lower() in ("1", "true", "yes")
ALIBABA_CLOUD_ACCESS_KEY_ID = os.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID", "")
ALIBABA_CLOUD_ACCESS_KEY_SECRET = os.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET", "")
ALIYUN_SMS_SIGN_NAME = os.getenv("ALIYUN_SMS_SIGN_NAME", "")
ALIYUN_SMS_TEMPLATE_CODE = os.getenv("ALIYUN_SMS_TEMPLATE_CODE", "")
SMS_ENDPOINT = os.getenv("ALIYUN_SMS_ENDPOINT", "dypnsapi.aliyuncs.com")
SMS_TEST_NUMBERS = os.getenv("SMS_TEST_NUMBERS", "")
SMS_TEMPLATE_PARAM_MIN = os.getenv("SMS_TEMPLATE_PARAM_MIN", "5")

os.makedirs(DATA_DIR, exist_ok=True)
os.makedirs(AUDIO_DIR, exist_ok=True)

app = FastAPI()
api = APIRouter(prefix="/voiceaid")


def now_ts():
    return datetime.utcnow().isoformat() + "Z"


def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    conn = get_db()
    cur = conn.cursor()
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE,
            password TEXT,
            bind TEXT UNIQUE,
            created_at TEXT
        )
        """
    )
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS evaluations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            uid INTEGER,
            info TEXT,
            evaluations TEXT,
            created_at TEXT
        )
        """
    )
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS modules (
            uid INTEGER PRIMARY KEY,
            module TEXT,
            updated_at TEXT
        )
        """
    )
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS audio (
            uid INTEGER,
            childUserID INTEGER,
            title TEXT,
            num TEXT,
            path TEXT,
            PRIMARY KEY (uid, childUserID, title, num)
        )
        """
    )
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS captcha_codes (
            contact TEXT,
            purpose TEXT,
            code TEXT,
            expires_at INTEGER,
            created_at TEXT,
            PRIMARY KEY (contact, purpose)
        )
        """
    )
    conn.commit()
    conn.close()


def parse_child_user(child_user_raw):
    try:
        obj = json.loads(child_user_raw)
    except Exception:
        return "{}", child_user_raw

    info = obj.get("info") or obj.get("Info") or {}
    evaluations = obj.get("evaluations") or obj.get("Evaluations") or obj
    return json.dumps(info, ensure_ascii=True), json.dumps(evaluations, ensure_ascii=True)


def evaluation_row_to_dict(row):
    def safe_json(text, fallback):
        try:
            return json.loads(text) if text else fallback
        except Exception:
            return fallback

    return {
        "ID": str(row["id"]),
        "info": safe_json(row["info"], {}),
        "evaluations": safe_json(row["evaluations"], {}),
        "UserID": str(row["uid"]),
        "Time": row["created_at"],
    }


@api.get("/health")
def health():
    return {"ok": True}


def is_phone_number(contact: str) -> bool:
    return re.fullmatch(r"\+?\d{7,15}", contact or "") is not None


def generate_captcha_code() -> str:
    return f"{random.randint(0, 999999):06d}"


def captcha_purpose_from_code(code: str):
    mapping = {
        "0": "login",
        "1": "register",
        "2": "change_password",
        "3": "logoff",
    }
    return mapping.get(code)


def save_captcha(contact: str, purpose: str, code: str):
    expires_at = int(time.time()) + CAPTCHA_TTL_SECONDS
    conn = get_db()
    cur = conn.cursor()
    cur.execute(
        "INSERT OR REPLACE INTO captcha_codes (contact, purpose, code, expires_at, created_at) VALUES (?, ?, ?, ?, ?)",
        (contact, purpose, code, expires_at, now_ts()),
    )
    conn.commit()
    conn.close()


def consume_captcha(contact: str, purpose: str, code: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute(
        "SELECT code, expires_at FROM captcha_codes WHERE contact=? AND purpose=?",
        (contact, purpose),
    )
    row = cur.fetchone()
    if not row:
        conn.close()
        return False, "captcha not found"
    if int(row["expires_at"]) < int(time.time()):
        cur.execute("DELETE FROM captcha_codes WHERE contact=? AND purpose=?", (contact, purpose))
        conn.commit()
        conn.close()
        return False, "captcha expired"
    if row["code"] != code:
        conn.close()
        return False, "captcha incorrect"
    cur.execute("DELETE FROM captcha_codes WHERE contact=? AND purpose=?", (contact, purpose))
    conn.commit()
    conn.close()
    return True, "ok"


_sms_client = None


def get_sms_client():
    global _sms_client
    if _sms_client is not None:
        return _sms_client
    if not ALIBABA_CLOUD_ACCESS_KEY_ID or not ALIBABA_CLOUD_ACCESS_KEY_SECRET:
        return None
    try:
        from alibabacloud_tea_openapi import models as open_api_models
        from alibabacloud_dypnsapi20170525.client import Client as Dypnsapi20170525Client
    except Exception:
        return None
    config = open_api_models.Config(
        access_key_id=ALIBABA_CLOUD_ACCESS_KEY_ID,
        access_key_secret=ALIBABA_CLOUD_ACCESS_KEY_SECRET,
        endpoint=SMS_ENDPOINT,
    )
    _sms_client = Dypnsapi20170525Client(config)
    return _sms_client


def send_sms_code(phone: str, code: str):
    if not SMS_ENABLED:
        return False, "sms disabled"
    if not ALIYUN_SMS_SIGN_NAME or not ALIYUN_SMS_TEMPLATE_CODE:
        return False, "sms config missing"
    client = get_sms_client()
    if client is None:
        return False, "sms client not initialized"
    try:
        from alibabacloud_dypnsapi20170525 import models as dypnsapi_20170525_models
        template_param = json.dumps({"code": code, "min": SMS_TEMPLATE_PARAM_MIN}, ensure_ascii=True)
        request = dypnsapi_20170525_models.SendSmsVerifyCodeRequest(
            sign_name=ALIYUN_SMS_SIGN_NAME,
            template_code=ALIYUN_SMS_TEMPLATE_CODE,
            phone_number=phone,
            template_param=template_param,
        )
        response = client.send_sms_verify_code(request)
        body = response.body or {}
        if getattr(body, "code", "") not in ("OK", "200", 200):
            return False, getattr(body, "message", "sms send failed")
        return True, "ok"
    except Exception as exc:
        return False, str(exc)


def normalize_phone_number(contact: str) -> str:
    cleaned = re.sub(r"[^0-9]", "", contact or "")
    if cleaned.startswith("86") and len(cleaned) > 11:
        return cleaned[2:]
    return cleaned


def get_sms_test_numbers():
    if not SMS_TEST_NUMBERS.strip():
        return set()
    items = [normalize_phone_number(x) for x in SMS_TEST_NUMBERS.split(",")]
    return {x for x in items if x}


@api.post("/get_captcha")
def get_captcha(code: str = Form(...), contact: str = Form(...)):
    purpose = captcha_purpose_from_code(code)
    if not purpose:
        return JSONResponse({"message": "invalid code"}, status_code=400)
    if not is_phone_number(contact):
        return JSONResponse({"message": "phone number required"}, status_code=400)
    test_numbers = get_sms_test_numbers()
    if test_numbers:
        if normalize_phone_number(contact) not in test_numbers:
            return JSONResponse({"message": "contact not in test list"}, status_code=400)
    captcha = generate_captcha_code()
    save_captcha(contact, purpose, captcha)
    ok, msg = send_sms_code(contact, captcha)
    if not ok:
        if SMS_DEBUG_RETURN_CODE:
            return {"message": "ok", "captcha": captcha, "debug": msg}
        # cleanup so failed sends do not leave valid codes behind
        consume_captcha(contact, purpose, captcha)
        return JSONResponse({"message": msg}, status_code=500)
    payload = {"message": "ok"}
    if SMS_DEBUG_RETURN_CODE:
        payload["captcha"] = captcha
    return payload


@api.post("/login")
def login(username: str = Form(...), password: str = Form(...)):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT id, username, password FROM users WHERE username=?", (username,))
    row = cur.fetchone()
    conn.close()
    if not row or row["password"] != password:
        return JSONResponse({"message": "username or password incorrect"}, status_code=400)
    return {"uid": str(row["id"]), "username": row["username"]}


@api.post("/login_captcha")
def login_captcha(bind: str = Form(...), captcha: str = Form(...)):
    ok, msg = consume_captcha(bind, "login", captcha)
    if not ok:
        return JSONResponse({"message": msg}, status_code=400)
    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT id, username FROM users WHERE bind=?", (bind,))
    row = cur.fetchone()
    conn.close()
    if not row:
        return JSONResponse({"message": "user not found"}, status_code=404)
    return {"uid": str(row["id"]), "username": row["username"]}


@api.post("/register")
def register(
    bind: str = Form(...),
    captcha: str = Form(...),
    username: str = Form(...),
    password: str = Form(...),
    password_confirm: str = Form(...),
):
    ok, msg = consume_captcha(bind, "register", captcha)
    if not ok:
        return JSONResponse({"message": msg}, status_code=400)
    if password != password_confirm:
        return JSONResponse({"message": "password confirm mismatch"}, status_code=400)

    conn = get_db()
    cur = conn.cursor()
    try:
        cur.execute(
            "INSERT INTO users (username, password, bind, created_at) VALUES (?, ?, ?, ?)",
            (username, password, bind, now_ts()),
        )
        conn.commit()
    except sqlite3.IntegrityError:
        conn.close()
        return JSONResponse({"message": "username or bind already exists"}, status_code=400)
    conn.close()
    return {"message": "ok"}


@api.post("/change_password")
def change_password(
    bind: str = Form(...),
    captcha: str = Form(...),
    password: str = Form(...),
    password_confirm: str = Form(...),
):
    ok, msg = consume_captcha(bind, "change_password", captcha)
    if not ok:
        return JSONResponse({"message": msg}, status_code=400)
    if password != password_confirm:
        return JSONResponse({"message": "password confirm mismatch"}, status_code=400)
    conn = get_db()
    cur = conn.cursor()
    cur.execute("UPDATE users SET password=? WHERE bind=?", (password, bind))
    if cur.rowcount == 0:
        conn.close()
        return JSONResponse({"message": "user not found"}, status_code=404)
    conn.commit()
    conn.close()
    return {"message": "ok"}


@api.post("/logoff_user")
def logoff_user(uid: str = Form(...), bind: str = Form(...), captcha: str = Form(...)):
    ok, msg = consume_captcha(bind, "logoff", captcha)
    if not ok:
        return JSONResponse({"message": msg}, status_code=400)
    conn = get_db()
    cur = conn.cursor()
    cur.execute("DELETE FROM users WHERE id=? AND bind=?", (uid, bind))
    if cur.rowcount == 0:
        conn.close()
        return JSONResponse({"message": "user not found"}, status_code=404)
    conn.commit()
    conn.close()
    return {"message": "ok"}


@api.post("/upload_evaluations")
def upload_evaluations(uid: str = Form(...), childUser: str = Form(...)):
    info, evaluations = parse_child_user(childUser)
    conn = get_db()
    cur = conn.cursor()
    cur.execute(
        "INSERT INTO evaluations (uid, info, evaluations, created_at) VALUES (?, ?, ?, ?)",
        (uid, info, evaluations, now_ts()),
    )
    child_user_id = cur.lastrowid
    conn.commit()
    conn.close()
    return {"childUserID": str(child_user_id)}


@api.post("/update_evaluation")
def update_evaluation(uid: str = Form(...), childUserID: str = Form(...), childUser: str = Form(...)):
    info, evaluations = parse_child_user(childUser)
    conn = get_db()
    cur = conn.cursor()
    cur.execute(
        "UPDATE evaluations SET info=?, evaluations=? WHERE id=? AND uid=?",
        (info, evaluations, childUserID, uid),
    )
    if cur.rowcount == 0:
        conn.close()
        return JSONResponse({"message": "evaluation not found"}, status_code=404)
    conn.commit()
    conn.close()
    return {"message": "ok"}


@api.get("/get_evaluations/{uid}")
def get_evaluations(uid: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT * FROM evaluations WHERE uid=? ORDER BY id DESC", (uid,))
    rows = cur.fetchall()
    conn.close()
    data = [evaluation_row_to_dict(r) for r in rows]
    return {"evaluations": data}


@api.get("/get_evaluations_limit/{uid}/{start}/{num}")
def get_evaluations_limit(uid: str, start: int, num: int):
    if start < 0 or num < 1:
        return JSONResponse({"message": "invalid range"}, status_code=400)
    conn = get_db()
    cur = conn.cursor()
    cur.execute(
        "SELECT * FROM evaluations WHERE uid=? ORDER BY id DESC LIMIT ? OFFSET ?",
        (uid, num, start),
    )
    rows = cur.fetchall()
    conn.close()
    data = [evaluation_row_to_dict(r) for r in rows]
    return {"evaluations": data}


@api.get("/get_evaluation/{uid}/{childUserID}")
def get_evaluation(uid: str, childUserID: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT * FROM evaluations WHERE id=? AND uid=?", (childUserID, uid))
    row = cur.fetchone()
    conn.close()
    if not row:
        return JSONResponse({"message": "evaluation not found"}, status_code=404)
    data = evaluation_row_to_dict(row)
    return {"evaluation": data}


@api.get("/get_evaluation_ids/{uid}")
def get_evaluation_ids(uid: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT id FROM evaluations WHERE uid=? ORDER BY id ASC", (uid,))
    ids = [r["id"] for r in cur.fetchall()]
    conn.close()
    return {"evaluationIDs": json.dumps(ids, ensure_ascii=True)}


@api.delete("/delete_evaluations/{admin}/{uid}")
def delete_evaluations(admin: str, uid: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("DELETE FROM evaluations WHERE uid=?", (uid,))
    conn.commit()
    conn.close()
    return {"message": "ok"}


@api.delete("/delete_evaluation/{uid}/{childUserID}")
def delete_evaluation(uid: str, childUserID: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("DELETE FROM evaluations WHERE id=? AND uid=?", (childUserID, uid))
    conn.commit()
    conn.close()
    return {"message": "ok"}


@api.get("/get_uids/{adminUid}")
def get_uids(adminUid: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT id FROM users WHERE id != ? ORDER BY id ASC", (adminUid,))
    ids = [r["id"] for r in cur.fetchall()]
    conn.close()
    return {"uids": json.dumps(ids, ensure_ascii=True)}


@api.get("/get_user_info/{uid}")
def get_user_info(uid: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT * FROM users WHERE id=?", (uid,))
    row = cur.fetchone()
    conn.close()
    if not row:
        return JSONResponse({"message": "user not found"}, status_code=404)
    data = {
        "ID": str(row["id"]),
        "Username": row["username"],
        "PassWord": row["password"],
        "Bind": row["bind"],
        "Time": row["created_at"],
    }
    return {"user": json.dumps(data, ensure_ascii=True)}


@api.delete("/delete_user_admin/{admin}/{uid}")
def delete_user_admin(admin: str, uid: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("DELETE FROM users WHERE id=?", (uid,))
    conn.commit()
    conn.close()
    return {"message": "ok"}


@api.post("/upload_audio")
def upload_audio(
    uid: str = Form(...),
    childUserID: str = Form(...),
    title: str = Form(...),
    num: str = Form(...),
    audio: UploadFile = File(...),
):
    user_dir = os.path.join(AUDIO_DIR, str(uid), str(childUserID), str(title))
    os.makedirs(user_dir, exist_ok=True)
    ext = os.path.splitext(audio.filename or "")[1] or ".amr"
    dst_path = os.path.join(user_dir, f"{num}{ext}")
    with open(dst_path, "wb") as f:
        f.write(audio.file.read())

    conn = get_db()
    cur = conn.cursor()
    cur.execute(
        "INSERT OR REPLACE INTO audio (uid, childUserID, title, num, path) VALUES (?, ?, ?, ?, ?)",
        (uid, childUserID, title, num, dst_path),
    )
    conn.commit()
    conn.close()
    return {"message": "ok"}


@api.get("/get_audio/{uid}/{childUserID}/{title}/{num}")
def get_audio(uid: str, childUserID: str, title: str, num: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute(
        "SELECT path FROM audio WHERE uid=? AND childUserID=? AND title=? AND num=?",
        (uid, childUserID, title, num),
    )
    row = cur.fetchone()
    conn.close()
    if not row or not os.path.exists(row["path"]):
        return JSONResponse({"message": "audio not found"}, status_code=404)
    with open(row["path"], "rb") as f:
        data = base64.b64encode(f.read()).decode("ascii")
    return {"audio": data}


@api.post("/create_module")
def create_module(uid: str = Form(...), module: str = Form(...)):
    conn = get_db()
    cur = conn.cursor()
    cur.execute(
        "INSERT OR REPLACE INTO modules (uid, module, updated_at) VALUES (?, ?, ?)",
        (uid, module, now_ts()),
    )
    conn.commit()
    conn.close()
    return {"message": "ok"}


@api.post("/update_module")
def update_module(uid: str = Form(...), module: str = Form(...)):
    try:
        incoming = json.loads(module)
    except Exception:
        incoming = {}

    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT module FROM modules WHERE uid=?", (uid,))
    row = cur.fetchone()
    if row:
        try:
            current = json.loads(row["module"] or "{}")
        except Exception:
            current = {}
        current.update(incoming)
        merged = json.dumps(current, ensure_ascii=True)
        cur.execute(
            "UPDATE modules SET module=?, updated_at=? WHERE uid=?",
            (merged, now_ts(), uid),
        )
    else:
        merged = json.dumps(incoming, ensure_ascii=True)
        cur.execute(
            "INSERT INTO modules (uid, module, updated_at) VALUES (?, ?, ?)",
            (uid, merged, now_ts()),
        )
    conn.commit()
    conn.close()
    return {"message": "ok"}


@api.get("/get_module/{uid}")
def get_module(uid: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT module FROM modules WHERE uid=?", (uid,))
    row = cur.fetchone()
    conn.close()
    module = row["module"] if row else "{}"
    return {"module": module}


@api.delete("/delete_module_admin/{admin}/{uid}")
def delete_module_admin(admin: str, uid: str):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("DELETE FROM modules WHERE uid=?", (uid,))
    conn.commit()
    conn.close()
    return {"message": "ok"}


app.include_router(api)
init_db()
