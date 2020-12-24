package greact.sample.plainjs.demo;

class Column<T> {
    @FunctionalInterface interface RowData<T> {
        Object fetch(T row);
    }

    final String header;
    final RowData<T> rowData;

    Column(String header, RowData<T> rowData) {
        this.header = header;
        this.rowData = rowData;
    }
}
