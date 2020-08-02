package common.battle.data;

public class DataAtk implements MaskAtk {
	public DataAtk(DefaultData data, int index) {
		this.index = index;
		this.data = data;
	}
	
	public final int index;
	public final DefaultData data;

	@Override
	public int getLongPoint() {
		return data.lds + data.ldr;
	}

	@Override
	public int[] getProc(int ind) {
		return data.proc[ind];
	}

	@Override
	public int getShortPoint() {
		return data.lds;
	}

	@Override
	public boolean isRange() {
		return data.isrange;
	}
	
	@Override
	public int getAtk() {
		switch(index) {
		case 0:
			return data.atk;
		case 1:
			return data.atk1;
		case 2:
			return data.atk2;
		default:
			return 0;
		}
	}
}