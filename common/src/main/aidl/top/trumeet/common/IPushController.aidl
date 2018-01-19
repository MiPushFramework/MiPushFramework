// IPushController.aidl
package top.trumeet.common;

// Control push service in app

interface IPushController {
   /** Get enable status of service
   * @param strict check all status
   **/
   boolean isEnable (in boolean strict);

   /** Set enable status of service **/
   void setEnable (in boolean enable);

   int getVersionCode ();

   int checkOp (int op);
}
