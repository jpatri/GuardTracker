package com.patri.guardtracker.sms;

import android.util.Log;

import java.util.Date;

/**
 * Created by patri on 22/09/2016.
 * Adapted from: http://stackoverflow.com/questions/848728/how-can-i-read-sms-messages-from-the-inbox-programmatically-in-android
 *
 */
public class Sms {
    private long    mId;
    private long    mThreadId;
    private String  mFrom;
    private String  mBody;
    private boolean mReadState;
    private Date    mDate;
    private String  mFolderName;
    private long    mContactId;

    public long     getId()             { return mId; }
    public String   getFrom()           { return mFrom; }
    public String   getBody()           { return mBody; }
    public boolean  getReadState()      { return mReadState; }
    public Date     getDate()           { return mDate; }
    public long     getThreadId()       { return mThreadId; }
    public long     getContactId()      { return mContactId; }
    public String   getFolderName()     { return mFolderName; }
    public String   getPrettyId()       { return "" + mId; }
    public String   getPrettyFrom()     { return mFrom; }
    public String   getPrettyBody()     { return mBody; }
    public String   getPrettyReadState(){ return "" + mReadState; }
    public String   getPrettyDate()     { return String.format("%1$td/%1$tm/%1$tY %1$tR", mDate); }
    public String   getPrettyFolderName(){ return mFolderName; }
    public String   getPrettyThreadId() { return "" + mThreadId; }
    public String   getPrettyContactId(){ return "" + mContactId; }


    public void setId(String id)                { setId(Long.parseLong(id)); }
    public void setId(long id)                  { mId = id; }
    public void setFrom(String from)            { mFrom = from; }
    public void setBody(String msg)             { mBody = msg; }
    public void setReadState(String readState)  { setReadState(Boolean.parseBoolean(readState)); }
    public void setReadState(boolean readState){
        mReadState = readState;
    }
    public void setDate(String dateStr)         { Log.i(this.getClass().getSimpleName(), dateStr); }
    public void setDate(long dateMiliseconds)   { setDate(new Date(dateMiliseconds)); }
    public void setDate(Date date)              { mDate = date; }
    public void setFolderName(String folderName){ mFolderName = folderName; }
    public void setThreadId(String threadId)    { setThreadId(Long.parseLong(threadId)); }
    public void setThreadId(long threadId)      { mThreadId = threadId; }
    public void setContactId(long contactId)    { mContactId = contactId; }
    public void setmContactId(String contactId) { setContactId(Long.parseLong(contactId)); }

}