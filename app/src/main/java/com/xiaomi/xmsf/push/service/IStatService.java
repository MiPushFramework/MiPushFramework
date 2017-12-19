package com.xiaomi.xmsf.push.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* compiled from: IStatService */
public interface IStatService extends IInterface {

    /* compiled from: IStatService */
    public static abstract class Stub extends Binder implements IStatService {

        @Override
        public IBinder asBinder() {
            return this;
        }

        public void insertEvent (String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.xiaomi.xmsf.push.service.IStatService");
                    obtain.writeString(str);
                    this.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            switch (i) {
                case 1:
                    parcel.enforceInterface("com.xiaomi.xmsf.push.service.IStatService");
                    insertEvent(parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 1598968902:
                    parcel2.writeString("com.xiaomi.xmsf.push.service.IStatService");
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    void insertEvent(String str) throws RemoteException;
}