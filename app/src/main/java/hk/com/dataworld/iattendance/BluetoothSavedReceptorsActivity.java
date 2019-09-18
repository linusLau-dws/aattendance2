package hk.com.dataworld.iattendance;

import android.content.ContentValues;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;

import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.filter.Filter;
import com.evrencoskun.tableview.filter.FilterChangedListener;

import java.util.ArrayList;
import java.util.List;

import static hk.com.dataworld.iattendance.SQLiteHelper.BD_Description;
import static hk.com.dataworld.iattendance.SQLiteHelper.BD_Name;

public class BluetoothSavedReceptorsActivity extends BaseActivity {

    private SQLiteHelper dbHelper;
    private TableView tableView;
    private Filter tableViewFilter;
    private BluetoothDeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.btn_attendanceCamel);
        setContentView(R.layout.activity_bluetooth_saved_receptors);


        adapter = new BluetoothDeviceAdapter(this);
        List<CellModel> headings = new ArrayList<>();
        headings.add(new CellModel(getString(R.string.bluetooth_name)));
        headings.add(new CellModel(getString(R.string.bluetooth_description)));
//        headings.add(new CellModel(getString(R.string.bluetooth_address)));


        List<CellModel> rowHeadings = new ArrayList<>();
        rowHeadings.add(new CellModel("1"));
        rowHeadings.add(new CellModel("2"));


        List<List<CellModel>> cells = new ArrayList<>();
        dbHelper = new SQLiteHelper(this);
        dbHelper.openDB();
        ArrayList<ContentValues> contentValues = dbHelper.getReceptors();
        for (ContentValues c :
                contentValues) {
            List<CellModel> tmp = new ArrayList<>();
            tmp.add(new CellModel(c.getAsString(BD_Name)));
            tmp.add(new CellModel(c.getAsString(BD_Description)));
//            tmp.add(new CellModel(c.getAsString(BD_Address)));
            cells.add(tmp);
        }
        dbHelper.closeDB();

        adapter.setAllItems(headings, rowHeadings, cells);

        tableView = findViewById(R.id.testTableView);
        tableView.setAdapter(adapter);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        tableView.setTranslationX(-180);
        tableView.getLayoutParams().width = displayMetrics.widthPixels + 180;

        tableViewFilter = new Filter(tableView);
        tableViewFilter.set(0, "Fi");

        BootstrapEditText editText = findViewById(R.id.filterEditText);
        tableView.getFilterHandler().addFilterChangedListener(new FilterChangedListener() {
            @Override
            public void onFilterChanged(List filteredCellItems, List filteredRowHeaderItems) {
                super.onFilterChanged(filteredCellItems, filteredRowHeaderItems);
                Log.i("test", "ttesst");
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tableViewFilter.set(0, charSequence.toString());
                Log.i("charSeq", charSequence.toString());

//                tableViewFilter.set(charSequence.toString());
//                tableView.filter(tableViewFilter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        TableLayout tableLayout = findViewById(R.id.receptors_table);
//
//        dbHelper = new SQLiteHelper(this);
//        dbHelper.openDB();
//        ArrayList<ContentValues> contentValues = dbHelper.getReceptors();
//        for (ContentValues c :
//                contentValues) {
//            TableRow row = new TableRow(this);
//
//            TextView bd_name = new TextView(this);
//            bd_name.setText(c.getAsString(BD_Name));
//            TableRow.LayoutParams param = new TableRow.LayoutParams(
//                    TableRow.LayoutParams.WRAP_CONTENT,
//                    TableRow.LayoutParams.WRAP_CONTENT,
//                    1.0f
//            );
//            bd_name.setLayoutParams(param);
//
//            TextView bd_addr = new TextView(this);
//            bd_addr.setText(c.getAsString(BD_Address));
//            bd_addr.setLayoutParams(param);
//
//            row.addView(bd_name);
//            row.addView(bd_addr);
//            tableLayout.addView(row);
//        }
//        dbHelper.closeDB();
    }
}
