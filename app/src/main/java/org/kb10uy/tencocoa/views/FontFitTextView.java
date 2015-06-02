package org.kb10uy.tencocoa.views;

/**
 * https://gist.github.com/STAR-ZERO/2934490
 *
 */
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * �T�C�Y��������TextView
 *
 */
public class FontFitTextView extends TextView {

    /** �ŏ��̃e�L�X�g�T�C�Y */
    private static final float MIN_TEXT_SIZE = 10f;

    /**
     * �R���X�g���N�^
     * @param context
     */
    public FontFitTextView(Context context) {
        super(context);
    }

    /**
     * �R���X�g���N�^
     * @param context
     * @param attrs
     */
    public FontFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        resize();

    }

    /**
     * �e�L�X�g�T�C�Y����
     */
    private void resize() {

        Paint paint = new Paint();

        // View�̕�
        int viewWidth = this.getWidth();
        // �e�L�X�g�T�C�Y
        float textSize = getTextSize();

        // Paint�Ƀe�L�X�g�T�C�Y�ݒ�
        paint.setTextSize(textSize);
        // �e�L�X�g�̉����擾
        float textWidth = paint.measureText(this.getText().toString());

        while (viewWidth <  textWidth) {
            // �����Ɏ��܂�܂Ń��[�v

            if (MIN_TEXT_SIZE >= textSize) {
                // �ŏ��T�C�Y�ȉ��ɂȂ�ꍇ�͍ŏ��T�C�Y
                textSize = MIN_TEXT_SIZE;
                break;
            }

            // �e�L�X�g�T�C�Y���f�N�������g
            textSize--;

            // Paint�Ƀe�L�X�g�T�C�Y�ݒ�
            paint.setTextSize(textSize);
            // �e�L�X�g�̉������Ď擾
            textWidth = paint.measureText(this.getText().toString());

        }

        // �e�L�X�g�T�C�Y�ݒ�
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

}