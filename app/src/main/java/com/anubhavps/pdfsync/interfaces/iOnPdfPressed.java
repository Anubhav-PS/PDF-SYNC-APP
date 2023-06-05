package com.anubhavps.pdfsync.interfaces;

import com.anubhavps.pdfsync.models.PDF;

public interface iOnPdfPressed {
    void onPdfCardPressed(String url);
    void onPdfDetailsPressed(PDF pdf);

}
