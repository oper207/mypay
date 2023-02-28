package kr.co.softrain.mypaytest.data.ability;

import kr.co.softrain.mypaytest.utils.ByteUtil;
import kr.co.softrain.mypaytest.utils.StringUtil;

/**
 * 암호화하지 않은 번호
 */
public class PlainNumAbility extends BaseAbility {

    private final byte[] amount = StringUtil.getLPadZero(9, "1004").getBytes();  // 1004?
    private final byte[] msg = StringUtil.getRPadSpace(16, "").getBytes();

    public PlainNumAbility(byte[] requestCode) {
        super(requestCode);
    }

    @Override
    public byte[] create() {
        return ByteUtil.mergeArrays(
                requestCode,
                amount,
                msg,
                cr
        );
    }
}
