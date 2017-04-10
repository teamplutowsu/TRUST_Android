package edu.wsu.eecs.pluto.trust.database;

/**
 * Created by Kameron on 2/6/2017.
 */

public class Module {

    private  String _module_num;
    private String _title;
    private String _npages;
    private String _imgFile;
    private String _lastPage;

    public Module(String moduleNum, String title, String nPages, String imgFile, String lastPage){
        _module_num = moduleNum;
        _title = title;
        _npages = nPages;
        _imgFile = imgFile;
        _lastPage = lastPage;

    }

    public String getModuleNum() {
        return _module_num;
    }
    public String getTitle() { return _title; }
    public String getNpages() { return _npages; }
    public String getImgFile() { return _imgFile; }
    public String getLastPage() { return _lastPage; }
}
