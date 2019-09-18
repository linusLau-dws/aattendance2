package hk.com.dataworld.iattendance;

import com.evrencoskun.tableview.filter.IFilterableModel;
import com.evrencoskun.tableview.sort.ISortableModel;

public class CellModel implements ISortableModel, IFilterableModel {

    private String mData;

    public CellModel(String mData) {
        this.mData = mData;
    }

    public String getData() {
        return mData;
    }

    @Override
    public String getId() {
        return mData;
    }

    @Override
    public Object getContent() {
        return mData;
    }

    @Override
    public String getFilterableKeyword() {
        return mData;
    }
}