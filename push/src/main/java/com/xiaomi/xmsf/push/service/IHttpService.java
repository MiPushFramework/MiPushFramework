package com.xiaomi.xmsf.push.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.util.Map;

public interface IHttpService extends IInterface {

    public static abstract class Stub extends Binder implements IHttpService {
        public Stub() {
            attachInterface(this, "com.xiaomi.xmsf.push.service.IHttpService");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            switch (i) {
                case 1:
                    parcel.enforceInterface("com.xiaomi.xmsf.push.service.IHttpService");
                    String doHttpPost = doHttpPost(parcel.readString(), parcel.readHashMap(getClass().getClassLoader()));
                    parcel2.writeNoException();
                    parcel2.writeString(doHttpPost);
                    return true;
                case 1598968902:
                    parcel2.writeString("com.xiaomi.xmsf.push.service.IHttpService");
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    String doHttpPost(String str, Map map) throws RemoteException;
}
