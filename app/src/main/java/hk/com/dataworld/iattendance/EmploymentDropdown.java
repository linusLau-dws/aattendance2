package hk.com.dataworld.iattendance;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.beardedhen.androidbootstrap.BootstrapDropDown;
import com.beardedhen.androidbootstrap.api.defaults.ExpandDirection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmploymentDropdown extends BootstrapDropDown {
    private JSONArray mJsonUnregd = new JSONArray();
    private JSONArray mJsonRegd = new JSONArray();

    private int mSelectedPos;

    private String mSelectedEmployment;
    private String mSelectedEnglishName;
    private String mSelectedChineseName;

    private boolean mIsRegd;

    public EmploymentDropdown(Context context) {
        super(context);
    }

    public EmploymentDropdown(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmploymentDropdown(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public String getmSelectedEmployment() {
        return mSelectedEmployment;
    }

    public String getmSelectedEnglishName() {
        return mSelectedEnglishName;
    }

    public String getmSelectedChineseName() {
        return mSelectedChineseName;
    }

    public boolean isDataSet() {
        if (mJsonRegd == null) {
            return false;
        } else {
            return true;
        }
    }

    public void debug() {
        Log.i("regd", mJsonRegd.toString());
        Log.i("not_regd", mJsonUnregd.toString());
    }

    public int getRegdNumberOfRows() {
        return mJsonRegd.length();
    }

    public void setJSONArray(JSONArray not_regd, JSONArray regd) {
        mJsonUnregd = not_regd;
        mJsonRegd = regd;

        String[] arr_regd = new String[not_regd.length()];
        for (int i = 0; i < not_regd.length(); i++) {
            JSONObject obj = null;
            try {
                obj = not_regd.getJSONObject(i);
                arr_regd[i] = String.format("%s %s (%s)", obj.getString("English Name"), obj.getString("Chinese Name"),
                        obj.getString("Employment Number"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setDropdownData(arr_regd);

        setOnDropDownItemClickListener(new OnDropDownItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View v, int id) {
                try {
                    mSelectedPos = id;
                    JSONObject obj = mJsonUnregd.getJSONObject(id);
                    mSelectedEmployment = obj.getString("Employment Number");
                    mSelectedEnglishName = obj.getString("English Name");
                    mSelectedChineseName = obj.getString("Chinese Name");
                    setText(String.format("%s %s (%s)", obj.getString("English Name"), obj.getString("Chinese Name"),
                            obj.getString("Employment Number")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void movedToUnassigned(String serial) {
        //TODO
        JSONObject found = null;
        for (int i = 0; i < mJsonRegd.length(); i++) {
            try {
                JSONObject obj = mJsonRegd.getJSONObject(i);
                if (serial.equals(obj.getString("Serial"))) {
                    mJsonRegd.remove(i);
                    Log.d("Removed",serial);
                    found = obj;
                    found.remove("Serial");
                    mJsonUnregd.put(found);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        propagateUnregd();
    }

    public JSONObject movedToAssigned(String cardID) {
        JSONObject obj = null;
        try {
            //Enlarge list
            obj = mJsonUnregd.getJSONObject(mSelectedPos);
            obj.put("Serial", cardID);
            obj.put("program", 1);
            mJsonRegd.put(obj);

            removeCurrent();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public JSONObject updateAssigned(String serial, String newEmploymentNumber) {
        JSONObject obj = null;
        try {
            for (int i = 0; i < mJsonRegd.length(); i++) {
                obj = mJsonRegd.getJSONObject(i);
                if (serial.equals(obj.getString("Serial"))) {
                    obj.remove("Employment Number");
                    obj.put("Employment Number", newEmploymentNumber);
                    mJsonRegd.remove(i);
                    mJsonRegd.put(i,obj);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void removeCurrent() {
        mJsonUnregd.remove(mSelectedPos);
        propagateUnregd();
    }

    private void propagateUnregd() {
        String[] arr_unregd = new String[mJsonUnregd.length()];
        for (int i = 0; i < mJsonUnregd.length(); i++) {
            JSONObject obj;
            try {
                obj = mJsonUnregd.getJSONObject(i);
                arr_unregd[i] = String.format("%s %s (%s)", obj.getString("English Name"), obj.getString("Chinese Name"),
                        obj.getString("Employment Number"));
            } catch (JSONException e) {
                e.printStackTrace();
                arr_unregd[i] = " ";
            }
        }
        Log.d("ToBeSet", Integer.toString(arr_unregd.length));

        setDropdownData(arr_unregd);    //TODO: Bugd
    }

    public void switchMode(boolean isRegd) {
        mIsRegd = isRegd;
        if (isRegd) {
            String[] arr = new String[mJsonUnregd.length() + mJsonRegd.length()];
            for (int i = 0; i < mJsonUnregd.length(); i++) {
                JSONObject obj;
                try {
                    obj = mJsonUnregd.getJSONObject(i);
                    arr[i] = String.format("%s %s (%s)", obj.getString("English Name"), obj.getString("Chinese Name"),
                            obj.getString("Employment Number"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < mJsonRegd.length(); i++) {
                JSONObject obj;
                try {
                    obj = mJsonRegd.getJSONObject(mJsonUnregd.length() + i);
                    arr[i] = String.format("%s %s (%s)", obj.getString("English Name"), obj.getString("Chinese Name"),
                            obj.getString("Employment Number"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setDropdownData(arr);
        } else {
            String[] arr = new String[mJsonUnregd.length()];
            for (int i = 0; i < mJsonUnregd.length(); i++) {
                JSONObject obj;
                try {
                    obj = mJsonUnregd.getJSONObject(i);
                    arr[i] = String.format("%s %s (%s)", obj.getString("English Name"), obj.getString("Chinese Name"),
                            obj.getString("Employment Number"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setDropdownData(arr);
        }
    }

    public String isSerialRegistered(String serial) {
        for (int i = 0; i < mJsonRegd.length(); i++) {
            try {
                JSONObject obj = mJsonRegd.getJSONObject(i);
                if (obj.getString("Serial").equals(serial)) {
                    return String.format("%s %s (%s)", obj.getString("English Name"), obj.getString("Chinese Name"),
                            obj.getString("Employment Number"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }


    @Override
    public void setOnDropDownItemClickListener(OnDropDownItemClickListener onDropDownItemClickListener) {
        super.setOnDropDownItemClickListener(onDropDownItemClickListener);
    }

    @Override
    public boolean isShowOutline() {
        return super.isShowOutline();
    }

    @Override
    public boolean isRounded() {
        return super.isRounded();
    }

    @Override
    public ExpandDirection getExpandDirection() {
        return super.getExpandDirection();
    }

    @Override
    public String[] getDropdownData() {
        return super.getDropdownData();
    }

    @Override
    public void setShowOutline(boolean showOutline) {
        super.setShowOutline(showOutline);
    }

    @Override
    public void setRounded(boolean rounded) {
        super.setRounded(rounded);
    }

    @Override
    public void setExpandDirection(ExpandDirection expandDirection) {
        super.setExpandDirection(expandDirection);
    }

    @Override
    public void setDropdownData(String[] dropdownData) {
        super.setDropdownData(dropdownData);
    }

    @Override
    public void onDismiss() {
        super.onDismiss();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    public void setOnClickListener(OnClickListener clickListener) {
        super.setOnClickListener(clickListener);
    }
}
