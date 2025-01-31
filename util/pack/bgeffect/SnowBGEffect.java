package common.util.pack.bgeffect;

import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.util.Data;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class SnowBGEffect extends BackgroundEffect{
    private final double maxSlope = Math.tan(Math.toRadians(75));
    private final FakeImage snow;

    private final int sw;
    private final int sh;

    private final List<P> snowPosition = new ArrayList<>();
    private final List<P> initPos = new ArrayList<>();
    private final List<Byte> speed = new ArrayList<>();
    private final List<Double> slope = new ArrayList<>();
    private final Random r = new Random();

    private final List<Integer> capture = new ArrayList<>();

    public SnowBGEffect(FakeImage snow) {
        this.snow = snow;

        this.sw = (int) (snow.getWidth() * 1.8);
        this.sh = (int) (snow.getHeight() * 1.8);
    }

    @Override
    public void check() {
        snow.bimg();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, double siz, double midH) {

    }

    @Override
    public void postDraw(FakeGraphics g, P rect, double siz, double midH) {
        g.setComposite(FakeGraphics.TRANS, 127, 0);

        for(int i = 0; i < snowPosition.size(); i++) {
            g.drawImage(snow, convertP(snowPosition.get(i).x, siz) + (int) rect.x, (int) (snowPosition.get(i).y * siz - rect.y + midH * siz), sw * siz, sh * siz);
        }

        g.setComposite(FakeGraphics.DEF, 255, 0);
    }

    @Override
    public void update(int w, double h, double midH) {
        capture.clear();

        for(int i = 0; i < snowPosition.size(); i++) {
            if(snowPosition.get(i).y >= 1510 + sh || snowPosition.get(i).x < -sw || snowPosition.get(i).x >= w + battleOffset) {
                capture.add(i);
            } else {
                snowPosition.get(i).y += speed.get(i);
                //slope(y - initY) + initX = x
                snowPosition.get(i).x = revertP(slope.get(i) * (snowPosition.get(i).y - initPos.get(i).y)) + initPos.get(i).x;
            }
        }

        if(!capture.isEmpty()) {
            for(int i = 0; i < capture.size(); i++) {
                double x = r.nextInt(w + sw + battleOffset);
                double y = -sh;

                snowPosition.get(capture.get(i)).x = x;
                snowPosition.get(capture.get(i)).y = y;
                initPos.get(capture.get(i)).x = x;
                initPos.get(capture.get(i)).y = y;

                //0 ~ 75
                double angle = Math.toRadians(r.nextInt(75));

                //-0.5angle + 1 is stabilizer
                speed.set(capture.get(i), (byte) ((Data.BG_EFFECT_SNOW_SPEED - r.nextInt(Data.BG_EFFECT_SNOW_SPEED - 3)) * (-0.75 * angle / maxSlope + 1)));
                slope.set(capture.get(i), Math.tan(-angle));
            }
        }
    }

    @Override
    public void initialize(int w, double h, double midH, Background bg) {
        for(int i = 0; i < snowPosition.size(); i++) {
            P.delete(snowPosition.get(i));
        }

        snowPosition.clear();

        int number = w / 200;

        for(int i = 0; i < number; i++) {
            double x = r.nextInt(w + sw + battleOffset);
            double y = r.nextInt(1510 + sh);
            snowPosition.add(P.newP(x, y));
            initPos.add(P.newP(x, y));

            //0~75
            double angle = Math.toRadians(r.nextInt(75));

            //-0.5angle + 1 is stabilizer
            speed.add((byte) ((Data.BG_EFFECT_SNOW_SPEED - r.nextInt(Data.BG_EFFECT_SNOW_SPEED - 3)) * (-0.75 * angle / maxSlope + 1)));
            slope.add(Math.tan(-angle));
        }
    }
}
