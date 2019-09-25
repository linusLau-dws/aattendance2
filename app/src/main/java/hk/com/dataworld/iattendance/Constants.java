package hk.com.dataworld.iattendance;

class Constants {
    static final String
            PREF_FIRST_RUN = "first_run",
            PREF_SERVER_ADDRESS = "pref_server_addr",
            PREF_ITEM = "item",
            PREF_LOCALE = "pref_locale",
            PREF_UNAME = "uname",
            PREF_HASH = "hash",
            PREF_TOKEN = "tok",
            PREF_REFRESH_TOKEN = "rtok",
            PREF_HAS_SUPERVISOR_RIGHT = "has_su",
            PREF_HAS_BLUETOOTH = "has_bluetooth",
            PREF_HAS_NFC ="has_su",
            PREF_HAS_QRCODE = "has_qrcode",
            ERR_VALID_EMPLOYMENT_NOT_FOUND = "ERROR_VALID_EMPLOYMENT_NOTFOUND",
    //EXTRA_HAS_EMPLOYMENTS = "has_employments",
    EXTRA_ALLOWED_APPROVALS = "allowed_approvals",
            EXTRA_BROADCAST_NOTIFICATION = "notification",
            EXTRA_BROADCAST_NOTIFICATION_COUNT = "notification_count",
            EXTRA_SOURCE_NOTIFICATION_STATUS = "status_code",
            EXTRA_SHIM_NOTIFICATION = "shim_notification",
            EXTRA_IS_ALL = "IsAll",
            EXTRA_TO_MY_HISTORY = "to_my_history",

    EXTRA_BACK_FROM_LEAVE_APPROVAL_DETAIL = "BackFromLeaveApprovalDetail",
            EXTRA_BACK_FROM_DETAIL_CONFIRM = "BackFromDetailConfirm",

    EXTRA_BACK_FROM_LEAVE_APPROVAL_STATUS = "BackFromLeaveApprovalStatus",
            EXTRA_WORKFLOW_STEPS = "workflow_steps",

    ACTION_INCOMING_NOTIFICATION = "incoming_notification",
            ACTION_ALTERING_COUNT = "altering_count";

    static final String DEBUG_FALLBACK_URL = "192.168.9.23:8888",
            NOTIFICATION_CHANNEL_NAME = "LEAVES_CHANNEL";

    static final String MAGIC_WORD = "getpw";

    static final int STATUS_WAITING = 1,
            STATUS_APPROVED = 2,
            STATUS_REJECTED = 3,
            STATUS_CANCELLED = 4,
            STATUS_PENDING_CANCEL = 5,
            STATUS_CONFIRMED_CANCELLED = 6,
            STATUS_RELEGATED = 7,
            NOTIFICATION_STATUS_APPROVER = 8;

    static final int LONGEST_TIMEOUT_MS = 60000, // 1 min
            MY_PERMISSIONS_REQUEST_FINE_LOCATION = 100,
            SCAN_TIMEOUT_SECONDS = 20;
}
