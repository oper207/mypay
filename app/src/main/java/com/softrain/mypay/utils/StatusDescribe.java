package com.softrain.mypay.utils;


/**
 * 사용법
 * String stateStr = StatusDescribe.getStatusDescribe(mPrinter.getStatus());
 */


// 프린터 상태를 나타내는 정수를 입력으로 사용 하고 상태를 설명하는 문자열을 반환
// 코드는 제어 채널을 통해 프린터와 상호 작용하여 프린터의 상태를 확인하고 해당 상태에 대해 사람이 읽을 수 있는 설명을 제공하도록 설계
public class StatusDescribe {

    /**
     * @param getStatus getState参数说明：
     *                  (getState & 0x01) >0 : 脱机
     *                  (getState & 0x02) >0 : 按纸键接通
     *                  (getState & 0x04) >0 : 发送错误
     *                  (getState & 0x08) >0 : 打印纸用完，停止打印
     *                  (getState & 0x10) >0 : 通过进纸键进纸
     *                  (getState & 0x20) >0 : 机头抬杠已打开
     *                  (getState & 0x40) >0 : 出现可自动恢复的错误
     *                  (getState & 0x80) >0 : 出现不可恢复的错误
     *                  (getState & 0x100) >0 : 发生机械错误
     *                  (getState & 0x200) >0 : 纸将尽检测器检测到纸张接近末端
     *                  (getState & 0x400) >0 : 纸尽传感器检测到卷纸末端
     * @return
     */
    // 입력의 비트 값을 기반으로 프린터 상태에 대한 설명을 제공합니다 getStatus. 각 비트는 프린터가 오프라인인지, 용지가 없는지 또는 기계적 오류가 있는지와 같은 프린터 상태의 다른 측면을 나타냅니다. 이 메서드는 if 문을 사용하여 설정된 비트를 확인하고 적절한 설명을 문자열 버퍼에 추가
    public static String getStatusDescribe(int getStatus) {
        try {
            int status = getStatus;
            if (status == -1) {
                return "数据传输错误,请检查连接或者重新发送";
            }
            StringBuilder builder = new StringBuilder();
            StringBuffer descriptBuffer = new StringBuffer();
            StringBuffer troubleBuffer = new StringBuffer();
            //传感应状态
            if ((status & 0x200) > 0) {
                descriptBuffer.append("少纸, ");//[1]
                troubleBuffer.append("PaperFew|");
            }


            if ((status & 0x400) > 0 || (status & 0x08) > 0) {
                descriptBuffer.append("缺纸, ");
                troubleBuffer.append("OutOfPaper|");

            }
            //脱机状态
            if ((status & 0x4) > 0) {
                descriptBuffer.append("发生错误, ");
                troubleBuffer.append("happen error|");

            }
            if ((status & 0x20) > 0) {
                descriptBuffer.append("盖板打开, ");
                troubleBuffer.append("box open|");

            }
            //打印机状态
            if ((status & 0x1) > 0) {
                descriptBuffer.append("脱机, ");
                troubleBuffer.append("Offline|");

            }
            if ((status & 0x2) > 0 || (status & 0x10) > 0) {
                descriptBuffer.append("正在feed, ");//[8]
                troubleBuffer.append("feeding|");

            }

            //错误状态
            if ((status & 0x100) > 0) {
                descriptBuffer.append("机械错误, ");
                troubleBuffer.append("MachineError|");

            }


            if ((status & 0x40) > 0) {
                descriptBuffer.append("可自动恢复错误, ");
                troubleBuffer.append("CorrectingError|");

            }


            if ((status & 0x80) > 0) {
                descriptBuffer.append("不可恢复错误, ");
                troubleBuffer.append("NotCorrectError|");

            }

            String descript = descriptBuffer.toString().trim();

            if (!descript.isEmpty()) {
                descript = descript.substring(0, descript.length() - 1);
            } else {
                descript = "正常";
            }


            return descript;
        } catch (Exception e) {

            return "Offline123123";
        }
    }

    //解析用控制通道获取的状态
    /**
     * @param getStatus getState参数说明：
     *                  0x10位 机器选择位
     *                  0x08位 为打印状态正常
     *                  0x40位 PNE 纸将尽
     *                  0x02位  过温
     *                  0x04位  切刀错误
     *                  0x20位  PE 缺纸
     *                  0x80位 盖打开
     *                  0x01位 出纸器有纸
     * @return
     */
    // 다른 비트 값을 사용하여 프린터 상태를 나타냅니다. 이 방법은 또한 입력의 비트 값을 기반으로 프린터 상태에 대한 설명을 제공합니다 getStatus.
    //두 메서드 모두 현재 설정된 프린터 상태의 다양한 측면에 대한 설명으로 구성된 프린터 상태를 설명하는 문자열을 반환합니다. 아무 항목도 설정되지 않은 경우 문자열 "正常"(보통)이 반환
    public static String getStatusDescribeWithControl(int getStatus) {
        try {
            int status = getStatus;
            if (status == -1) {
                return "数据传输错误,请检查连接或者重新发送";
            }
            StringBuilder builder = new StringBuilder();
            StringBuffer descriptBuffer = new StringBuffer();
            StringBuffer troubleBuffer = new StringBuffer();
            //传感应状态
            if ((status & 0x40) > 0) {
                descriptBuffer.append("少纸, ");//[1]
                troubleBuffer.append("PaperFew|");

            }


            if ((status & 0x20) > 0) {
                descriptBuffer.append("缺纸, ");
                troubleBuffer.append("OutOfPaper|");

            }
            //脱机状态
            if ((status & 0x4) > 0) {
                descriptBuffer.append("发生错误, ");
                troubleBuffer.append("happen error|");

            }
            if ((status & 0x80) > 0) {
                descriptBuffer.append("盖板打开, ");
                troubleBuffer.append("box open|");

            }

            String descript = descriptBuffer.toString().trim();

            if (!descript.isEmpty()) {
                descript = descript.substring(0, descript.length() - 1);
            } else {
                descript = "正常";
            }


            return descript;
        } catch (Exception e) {

            return "Offline123123";
        }
    }
}