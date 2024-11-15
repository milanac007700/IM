package com.milanac007.demo.im.net;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.concurrent.Callable;

public class DownloadCallable implements Callable<JSONObject> {
    private final int fileType;
    private final String url;
    private final File saveFile;

    public DownloadCallable(int fileType, String url, File saveFile) {
        this.fileType = fileType;
        this.url = url;
        this.saveFile = saveFile;
    }

    @Override
    public JSONObject call() throws Exception {
        String result = new NetRequestByOkHttpClient().getFile(url, saveFile, -1, -1);
        return JSONObject.parseObject(result);
    }

}
