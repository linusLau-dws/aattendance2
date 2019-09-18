package hk.com.dataworld.iattendance;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evrencoskun.tableview.ITableView;
import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.evrencoskun.tableview.sort.SortState;

import androidx.core.content.ContextCompat;

public class BluetoothDeviceAdapter extends AbstractTableAdapter<CellModel, CellModel, CellModel> {
    private Context m_Context;

    public BluetoothDeviceAdapter(Context context) {
        super(context);
        m_Context = context;
    }

    //    @Override
//    public void setTableView(TableView tableView) {
//        super.setTableView(tableView);
//    }
//
//    @Override
//    public void setColumnHeaderItems(List<String> columnHeaderItems) {
//        //super.setColumnHeaderItems(columnHeaderItems);
//    }
//
//    @Override
//    public void setRowHeaderItems(List<String> rowHeaderItems) {
//        //super.setRowHeaderItems(rowHeaderItems);
//    }
//
//    @Override
//    public void setCellItems(List<List<CellModel>> cellItems) {
//        super.setCellItems(cellItems);
//    }
//
//    @Override
//    public void setAllItems(List<String> columnHeaderItems, List<String> rowHeaderItems, List<List<CellModel>> cellItems) {
//        super.setAllItems(columnHeaderItems, rowHeaderItems, cellItems);
//    }
//
//    @Override
//    public View getCornerView() {
//        return super.getCornerView();
//    }
//
//    @Override
//    public ColumnHeaderRecyclerViewAdapter getColumnHeaderRecyclerViewAdapter() {
//        return super.getColumnHeaderRecyclerViewAdapter();
//    }
//
//    @Override
//    public RowHeaderRecyclerViewAdapter getRowHeaderRecyclerViewAdapter() {
//        return super.getRowHeaderRecyclerViewAdapter();
//    }
//
//    @Override
//    public CellRecyclerViewAdapter getCellRecyclerViewAdapter() {
//        return super.getCellRecyclerViewAdapter();
//    }
//
//    @Override
//    public void setRowHeaderWidth(int rowHeaderWidth) {
//        super.setRowHeaderWidth(rowHeaderWidth);
//    }
//
//    @Override
//    public void setColumnHeaderHeight(int columnHeaderHeight) {
//        super.setColumnHeaderHeight(columnHeaderHeight);
//    }
//
//    @Override
//    public String getColumnHeaderItem(int position) {
//        return super.getColumnHeaderItem(position);
//    }
//
//    @Override
//    public String getRowHeaderItem(int position) {
//        return super.getRowHeaderItem(position);
//    }
//
//    @Override
//    public CellModel getCellItem(int columnPosition, int rowPosition) {
//        return super.getCellItem(columnPosition, rowPosition);
//    }
//
//    @Override
//    public List<CellModel> getCellRowItems(int rowPosition) {
//        return super.getCellRowItems(rowPosition);
//    }
//
//    @Override
//    public void removeRow(int rowPosition) {
//        super.removeRow(rowPosition);
//    }
//
//    @Override
//    public void removeRow(int rowPosition, boolean updateRowHeader) {
//        super.removeRow(rowPosition, updateRowHeader);
//    }
//
//    @Override
//    public void removeRowRange(int rowPositionStart, int itemCount) {
//        super.removeRowRange(rowPositionStart, itemCount);
//    }
//
//    @Override
//    public void removeRowRange(int rowPositionStart, int itemCount, boolean updateRowHeader) {
//        super.removeRowRange(rowPositionStart, itemCount, updateRowHeader);
//    }
//
//    @Override
//    public void addRow(int rowPosition, String rowHeaderItem, List<CellModel> cellItems) {
//        super.addRow(rowPosition, rowHeaderItem, cellItems);
//    }
//
//    @Override
//    public void addRowRange(int rowPositionStart, List<String> rowHeaderItem, List<List<CellModel>> cellItems) {
//        super.addRowRange(rowPositionStart, rowHeaderItem, cellItems);
//    }
//
//    @Override
//    public void changeRowHeaderItem(int rowPosition, String rowHeaderModel) {
//        super.changeRowHeaderItem(rowPosition, rowHeaderModel);
//    }
//
//    @Override
//    public void changeRowHeaderItemRange(int rowPositionStart, List<String> rowHeaderModelList) {
//        super.changeRowHeaderItemRange(rowPositionStart, rowHeaderModelList);
//    }
//
//    @Override
//    public void changeCellItem(int columnPosition, int rowPosition, CellModel cellModel) {
//        super.changeCellItem(columnPosition, rowPosition, cellModel);
//    }
//
//    @Override
//    public void changeColumnHeader(int columnPosition, String columnHeaderModel) {
//        super.changeColumnHeader(columnPosition, columnHeaderModel);
//    }
//
//    @Override
//    public void changeColumnHeaderRange(int columnPositionStart, List<String> columnHeaderModelList) {
//        super.changeColumnHeaderRange(columnPositionStart, columnHeaderModelList);
//    }
//
//    @Override
//    public List<CellModel> getCellColumnItems(int columnPosition) {
//        return super.getCellColumnItems(columnPosition);
//    }
//
//    @Override
//    public void removeColumn(int columnPosition) {
//        super.removeColumn(columnPosition);
//    }
//
//    @Override
//    public void addColumn(int columnPosition, String columnHeaderItem, List<CellModel> cellItems) {
//        super.addColumn(columnPosition, columnHeaderItem, cellItems);
//    }
//
//    @Override
//    public ITableView getTableView() {
//        return super.getTableView();
//    }
//
//    @Override
//    public void addAdapterDataSetChangedListener(AdapterDataSetChangedListener listener) {
//        super.addAdapterDataSetChangedListener(listener);
//    }
//
    @Override
    public int getColumnHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public AbstractViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(m_Context).inflate(R.layout.layout_textview, parent, false);

        // Create the relevant view holder
        return new SortableViewHolder(layout);
    }

    @Override
    public void onBindCellViewHolder(AbstractViewHolder holder, Object cellItemModel, int columnPosition, int rowPosition) {
        CellModel cell = (CellModel) cellItemModel;
        ((SortableViewHolder) holder).setCellModel(cell.getData(), columnPosition);
    }

    @Override
    public AbstractViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(m_Context).inflate(R.layout.layout_header,
                parent, false);

        return new ColumnHeaderViewHolder(layout, getTableView());
    }

    @Override
    public int getRowHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public int getCellItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindColumnHeaderViewHolder(AbstractViewHolder holder, Object columnHeaderItemModel, int columnPosition) {
        CellModel columnHeader = (CellModel) columnHeaderItemModel;

        ColumnHeaderViewHolder columnHeaderViewHolder = (ColumnHeaderViewHolder) holder;
        columnHeaderViewHolder.setColumnHeaderModel(columnHeader.getData(), columnPosition);
    }

    @Override
    public AbstractViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(m_Context).inflate(R.layout.layout_row,
                parent, false);

//        return new RowHeaderViewHolder(layout);
        return new RowHeaderViewHolder(layout);
    }

    @Override
    public void onBindRowHeaderViewHolder(AbstractViewHolder holder, Object rowHeaderItemModel, int rowPosition) {
//        CellModel rowHeaderModel = (CellModel) rowHeaderItemModel;
//
//        RowHeaderViewHolder rowHeaderViewHolder = (RowHeaderViewHolder) holder;
//        rowHeaderViewHolder.row_header_textview.setText(rowHeaderModel.getData());
    }

    @Override
    public View onCreateCornerView() {
        return LayoutInflater.from(m_Context).inflate(R.layout.layout_row, null, false);
    }

    public class SortableViewHolder extends AbstractViewHolder {
        public final TextView cell_textview;
        public final LinearLayout cell_container;

        public SortableViewHolder(View itemView) {
            super(itemView);
            cell_textview = itemView.findViewById(R.id.txtView);
            cell_container = itemView.findViewById(R.id.cell_container);
        }

        public void setCellModel(String p_jModel, int pColumnPosition) {
            // Set text
            cell_textview.setText(String.valueOf(p_jModel));

            // It is necessary to remeasure itself.
            cell_container.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
            cell_textview.requestLayout();
        }

        @Override
        public void setSelected(SelectionState p_nSelectionState) {
            super.setSelected(p_nSelectionState);

            if (p_nSelectionState == SelectionState.SELECTED) {
                cell_textview.setTextColor(ContextCompat.getColor(cell_textview.getContext(),
                        android.R.color.white));
            } else {
                cell_textview.setTextColor(ContextCompat.getColor(cell_textview.getContext(),
                        android.R.color.black));
            }
        }
    }

    public class RowHeaderViewHolder extends AbstractViewHolder {
        //public final TextView row_header_textview;

        public RowHeaderViewHolder(View p_jItemView) {
            super(p_jItemView);
            //row_header_textview = p_jItemView.findViewById(R.id.row_header_textview);
        }

        @Override
        public void setSelected(SelectionState p_nSelectionState) {
            super.setSelected(p_nSelectionState);

//            int nBackgroundColorId;
//            int nForegroundColorId;
//
//            if (p_nSelectionState == SelectionState.SELECTED) {
//                nBackgroundColorId = R.color.bluetoothBlue;
//                nForegroundColorId = android.R.color.white;
//
//            } else if (p_nSelectionState == SelectionState.UNSELECTED) {
//                nBackgroundColorId = android.R.color.white;
//                nForegroundColorId = android.R.color.black;
//
//            } else { // SelectionState.SHADOWED
//
//                nBackgroundColorId = android.R.color.darker_gray;
//                nForegroundColorId = android.R.color.white;
//            }
//
//            itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(),
//                    nBackgroundColorId));
//            row_header_textview.setTextColor(ContextCompat.getColor(row_header_textview.getContext(),
//                    nForegroundColorId));
        }
    }

    public class ColumnHeaderViewHolder extends AbstractSorterViewHolder {
        public final int[] COLUMN_TEXT_ALIGNS = {
                // Id
                Gravity.CENTER,
                // Name
                Gravity.LEFT,
                // Nickname
                Gravity.LEFT,
                // Email
                Gravity.LEFT,
                // BirthDay
                Gravity.CENTER,
                // Gender (Sex)
                Gravity.CENTER,
                // Age
                Gravity.CENTER,
                // Job
                Gravity.LEFT,
                // Salary
                Gravity.CENTER,
                // CreatedAt
                Gravity.CENTER,
                // UpdatedAt
                Gravity.CENTER,
                // Address
                Gravity.LEFT,
                // Zip Code
                Gravity.RIGHT,
                // Phone
                Gravity.RIGHT,
                // Fax
                Gravity.RIGHT};
        final LinearLayout column_header_container;
        final TextView column_header_textview;
        final ImageButton column_header_sort_button;
        final ITableView tableView;
        private View.OnClickListener mSortButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (getSortState() == SortState.ASCENDING) {
                        tableView.sortColumn(getAdapterPosition(), SortState.DESCENDING);
                    } else if (getSortState() == SortState.DESCENDING) {
                        tableView.sortColumn(getAdapterPosition(), SortState.ASCENDING);
                    } else {
                        // Default one
                        tableView.sortColumn(getAdapterPosition(), SortState.DESCENDING);
                    }
                } catch (Exception e) {

                }
            }
        };

        public ColumnHeaderViewHolder(View itemView, ITableView pTableView) {
            super(itemView);
            tableView = pTableView;
            column_header_textview = itemView.findViewById(R.id.column_header_textView);
            column_header_container = itemView.findViewById(R.id.column_header_container);
            column_header_sort_button = itemView.findViewById(R.id.column_header_sort_imageButton);

            // Set click listener to the sort button
            column_header_sort_button.setOnClickListener(mSortButtonClickListener);
            column_header_textview.setOnClickListener(mSortButtonClickListener);
        }

        public void setColumnHeaderModel(String pColumnHeaderModel, int pColumnPosition) {

            // Change alignment of textView
            column_header_textview.setGravity(COLUMN_TEXT_ALIGNS[pColumnPosition] | Gravity
                    .CENTER_VERTICAL);

            // Set text data
            column_header_textview.setText(pColumnHeaderModel);

            // It is necessary to remeasure itself.
            column_header_container.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
            column_header_textview.requestLayout();
        }

        @Override
        public void setSelected(SelectionState p_nSelectionState) {
            super.setSelected(p_nSelectionState);
        }

        @Override
        public void onSortingStatusChanged(SortState pSortState) {
            super.onSortingStatusChanged(pSortState);

            // It is necessary to remeasure itself.
            column_header_container.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;

            controlSortState(pSortState);

            column_header_textview.requestLayout();
            column_header_sort_button.requestLayout();
            column_header_container.requestLayout();
            itemView.requestLayout();
        }

        private void controlSortState(SortState pSortState) {
            if (pSortState == SortState.ASCENDING) {
                column_header_sort_button.setVisibility(View.VISIBLE);
                column_header_sort_button.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);

            } else if (pSortState == SortState.DESCENDING) {
                column_header_sort_button.setVisibility(View.VISIBLE);
                column_header_sort_button.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);
            } else {
                column_header_sort_button.setVisibility(View.GONE);
            }
        }
    }
}
