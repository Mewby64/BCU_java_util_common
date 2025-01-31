package common.util.pack.bgeffect;

import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.FakeTransform;
import common.util.Data;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class BlizzardBGEffect extends BackgroundEffect {
    private final FakeImage blizzard;

    private final int bw;
    private final int bh;

    private final List<P> blizzardPosition = new ArrayList<>();
    private final List<P> initPos = new ArrayList<>();
    private final List<Double> slope = new ArrayList<>();
    private final List<Double> angle = new ArrayList<>();
    private final List<Byte> size = new ArrayList<>();
    private final List<Byte> speed = new ArrayList<>();
    private final Random r = new Random();

    private final List<Integer> capture = new ArrayList<>();

    public BlizzardBGEffect(FakeImage blizzard) {
        this.blizzard = blizzard;

        bw = this.blizzard.getWidth();
        bh = this.blizzard.getHeight();
    }

    @Override
    public void check() {
        blizzard.bimg();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, double siz, double midH) {

    }

    @Override
    public void postDraw(FakeGraphics g, P rect, double siz, double midH) {
        FakeTransform at = g.getTransform();

        for(int i = 0; i < blizzardPosition.size(); i++) {
            g.translate(convertP(blizzardPosition.get(i).x, siz) + rect.x, blizzardPosition.get(i).y * siz - rect.y + midH * siz);
            g.rotate(-angle.get(i));

            g.drawImage(blizzard, 0, 0, (int) (bw * 0.5 * Data.BG_EFFECT_BLIZZARD_SIZE[size.get(i)] * siz), (int) (bh * Data.BG_EFFECT_BLIZZARD_SIZE[size.get(i)] * siz));

            g.setTransform(at);
        }

        g.delete(at);
    }

    @Override
    public void update(int w, double h, double midH) {
        capture.clear();

        for(int i = 0; i < blizzardPosition.size(); i++) {
            if(blizzardPosition.get(i).x >= w + battleOffset || blizzardPosition.get(i).y >= BGHeight * 3 + bh * Data.BG_EFFECT_BLIZZARD_SIZE[size.get(i)]) {
                capture.add(i);
            } else {
                blizzardPosition.get(i).y += speed.get(i);
                blizzardPosition.get(i).x = revertP(slope.get(i) * (blizzardPosition.get(i).y - initPos.get(i).y)) + initPos.get(i).x;
            }
        }

        if(!capture.isEmpty()) {
            for(int i = 0; i < capture.size(); i++) {
                byte sizeIndex = (byte) Math.min(Data.BG_EFFECT_BLIZZARD_SIZE.length - 1, r.nextInt(Data.BG_EFFECT_BLIZZARD_SIZE.length));

                double x = r.nextDouble() * (w + battleOffset + bw * Data.BG_EFFECT_BLIZZARD_SIZE[sizeIndex] + revertP(BGHeight * 3)) - revertP(BGHeight * 3);
                double y = -bh * Data.BG_EFFECT_BLIZZARD_SIZE[sizeIndex];

                blizzardPosition.get(capture.get(i)).x = x;
                blizzardPosition.get(capture.get(i)).y = y;

                initPos.get(capture.get(i)).x = x;
                initPos.get(capture.get(i)).y = y;

                speed.set(capture.get(i), (byte) (Data.BG_EFFECT_BLIZZARD_SPEED - r.nextInt(5)));

                double a = Math.toRadians(60 - r.nextInt(15));

                angle.set(capture.get(i), a);
                slope.set(capture.get(i), Math.tan(a));
                size.set(capture.get(i), sizeIndex);
            }
        }
    }

    @Override
    public void initialize(int w, double h, double midH, Background bg) {
        for(int i = 0; i < blizzardPosition.size(); i++) {
            P.delete(blizzardPosition.get(i));
            P.delete(initPos.get(i));
        }

        blizzardPosition.clear();
        initPos.clear();
        speed.clear();
        slope.clear();
        size.clear();

        int number = w / 50;

        for(int i = 0; i < number; i++) {
            byte sizeIndex = (byte) Math.min(Data.BG_EFFECT_BLIZZARD_SIZE.length - 1, r.nextInt(Data.BG_EFFECT_BLIZZARD_SIZE.length));

            double x = r.nextDouble() * (w + battleOffset + bw * Data.BG_EFFECT_BLIZZARD_SIZE[sizeIndex] + revertP(BGHeight * 3)) - revertP(BGHeight * 3);
            double y = r.nextDouble() * (BGHeight * 3 + bh * Data.BG_EFFECT_BLIZZARD_SIZE[sizeIndex]);

            blizzardPosition.add(P.newP(x, y));
            initPos.add(P.newP(x, y));
            speed.add((byte) (Data.BG_EFFECT_BLIZZARD_SPEED - r.nextInt(5)));

            double a = Math.toRadians(60 - r.nextInt(15));

            angle.add(a);
            slope.add(Math.tan(a));
            size.add(sizeIndex);
        }
    }
}
