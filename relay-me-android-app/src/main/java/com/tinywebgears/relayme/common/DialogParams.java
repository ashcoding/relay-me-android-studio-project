package com.tinywebgears.relayme.common;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class DialogParams<T> implements Parcelable
{
    private static int TYPE_INT = 1;
    private static int TYPE_LONG = 2;
    private static int TYPE_STRING = 3;
    private int type;
    private String id;
    private T value;

    public static final Parcelable.Creator<DialogParams> CREATOR = new Creator<DialogParams>()
    {
        @Override
        public DialogParams createFromParcel(Parcel source)
        {
            return DialogParams.createFromParcel(source);
        }

        @Override
        public DialogParams[] newArray(int size)
        {
            return new DialogParams[size];
        }
    };

    public DialogParams()
    {
    }

    public DialogParams(int type, String id, T value)
    {
        this.type = type;
        this.id = id;
        this.value = value;
    }

    public String getId()
    {
        return id;
    }

    public T getValue()
    {
        return value;
    }

    // TODO: Later: Delegate this to the subclass
    public static DialogParams<?> createFromParcel(Parcel parcel)
    {
        int type = parcel.readInt();
        String id = parcel.readString();
        if (type == TYPE_INT)
            return new IntDialogExtra(id, parcel.readInt());
        else if (type == TYPE_LONG)
            return new LongDialogExtra(id, parcel.readLong());
        else if (type == TYPE_STRING)
            return new StringDialogExtra(id, parcel.readString());
        else
            throw new IllegalArgumentException("Invalid dialog extra type: " + type);
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeString(id);
        writeValue(parcel, value);
    }

    abstract protected void writeValue(Parcel parcel, T value);

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static class IntDialogExtra extends DialogParams<Integer>
    {
        public IntDialogExtra(String id, Integer value)
        {
            super(TYPE_INT, id, value);
        }

        @Override
        protected void writeValue(Parcel parcel, Integer value)
        {
            parcel.writeInt(value);
        }
    }

    public static class LongDialogExtra extends DialogParams<Long>
    {
        public LongDialogExtra(String id, Long value)
        {
            super(TYPE_LONG, id, value);
        }

        @Override
        protected void writeValue(Parcel parcel, Long value)
        {
            parcel.writeLong(value);
        }
    }

    public static class StringDialogExtra extends DialogParams<String>
    {
        public StringDialogExtra(String id, String value)
        {
            super(TYPE_STRING, id, value);
        }

        @Override
        protected void writeValue(Parcel parcel, String value)
        {
            parcel.writeString(value);
        }
    }
}
