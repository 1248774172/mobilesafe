package com.xiaoer.mobilesafe.db.dao.impl;

import android.content.Context;
import android.util.Log;

import androidx.test.InstrumentationRegistry;

import junit.framework.TestCase;

import static android.content.ContentValues.TAG;

public class BlackNumberDaoImplTest extends TestCase {

    public void testGetTotalCount() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        BlackNumberDaoImpl blackNumberDao = BlackNumberDaoImpl.getInstance(appContext);
        int totalCount = blackNumberDao.getTotalCount();
        Log.d(TAG, "testGetTotalCount: -----------------------------"+totalCount);

    }
}