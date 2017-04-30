package com.tinywebgears.relayme.service;

import java.util.HashMap;
import java.util.Map;

import android.app.IntentService;
import android.content.Intent;

import com.codolutions.android.common.util.NumberUtil;

public abstract class BasicIntentService extends IntentService
{
    private static final String TAG = BasicIntentService.class.getName();

    public static final String PARAM_ACTION_ID = "action-id";
    public static final String PARAM_OUT_RESULT = "result";
    public static final String PARAM_OUT_RANDOM_NUMBER = "timestamp";
    public static final String PARAM_OUT_ERROR_MESSAGE = "out-error-message";

    public BasicIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        String action = intent.getAction();
        long actionId = intent.getExtras().getLong(BasicIntentService.PARAM_ACTION_ID);
        ActionResponse result = handleAction(action, actionId, intent);
        ActionStatus status = result.status;
        ExtrasAdder extrasAdder = result.extrasAdder;
        sendActionReponseBroadcast(action, status, actionId, extrasAdder);
    }

    protected void reportOperationProgress(String action, long actionId, int count, int progressIncrement)
    {
    }

    private void sendActionReponseBroadcast(String action, ActionStatus status, long actionId, ExtrasAdder extrasAdder)
    {
        String responseAction = getResponseAction(action);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(responseAction);
        broadcastIntent.putExtra(BasicIntentService.PARAM_ACTION_ID, actionId);
        broadcastIntent.putExtra(BasicIntentService.PARAM_OUT_RESULT, status.toString());
        broadcastIntent.putExtra(BasicIntentService.PARAM_OUT_RANDOM_NUMBER, NumberUtil.getRandomNumber());
        if (extrasAdder != null)
            extrasAdder.addExtras(broadcastIntent);
        sendBroadcast(broadcastIntent);
    }

    abstract protected ActionResponse handleAction(String action, long actionId, Intent intent);

    abstract protected String getResponseAction(String requestAction);

    protected static class ActionResponse
    {
        protected static ActionResponse SUCCESS = new ActionResponse(ActionStatus.SUCCESS, null);
        protected static ActionResponse FAILURE = new ActionResponse(ActionStatus.FAILURE, null);
        protected static ActionResponse DROP = new ActionResponse(ActionStatus.IGNORE_RESULT, null);

        protected static ActionResponse SUCCESS_WITH_EXTRAS(ExtrasAdder extrasAdder)
        {
            return new ActionResponse(ActionStatus.SUCCESS, extrasAdder);
        }

        protected static ActionResponse FAILURE_WITH_EXTRAS(ExtrasAdder extrasAdder)
        {
            return new ActionResponse(ActionStatus.FAILURE, extrasAdder);
        }

        protected static ActionResponse FAILURE_WITH_ERROR_MESSAGE(final String errorMessage)
        {
            return ActionResponse.FAILURE_WITH_EXTRAS(new ExtrasAdder()
            {
                @Override
                public void addExtras(Intent intent)
                {
                    intent.putExtra(BasicIntentService.PARAM_OUT_ERROR_MESSAGE, errorMessage);
                }
            });
        }

        public final ActionStatus status;
        public final ExtrasAdder extrasAdder;

        public ActionResponse(ActionStatus status)
        {
            this(status, null);
        }

        public ActionResponse(ActionStatus status, ExtrasAdder extrasAdder)
        {
            this.status = status;
            this.extrasAdder = extrasAdder;
        }
    }

    public static enum ActionStatus
    {
        SUCCESS("success"), FAILURE("error"), IGNORE_RESULT("ignore-result");

        private final String value;

        private static final Map<String, ActionStatus> stringToEnum = new HashMap<String, ActionStatus>();

        static
        {
            for (ActionStatus e : values())
                stringToEnum.put(e.toString(), e);
        }

        private ActionStatus(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }

        public static ActionStatus defaultValue()
        {
            return IGNORE_RESULT;
        }

        public static ActionStatus fromString(String str)
        {
            return stringToEnum.containsKey(str) ? stringToEnum.get(str) : defaultValue();
        }
    }

    protected interface ExtrasAdder
    {
        void addExtras(Intent intent);
    }
}
