package common.pack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.gson.JsonElement;
import common.CommonStatic;
import common.battle.data.DataEnemy;
import common.battle.data.Orb;
import common.battle.data.PCoin;
import common.io.PackLoader.ZipDesc.FileDesc;
import common.io.assets.AssetLoader;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.io.json.JsonClass.JCConstructor;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonClass.RType;
import common.io.json.JsonField.GenType;
import common.pack.FixIndexList.FixIndexMap;
import common.pack.Source.Workspace;
import common.pack.VerFixer.IdFixer;
import common.system.files.FDFile;
import common.system.files.VFile;
import common.system.files.VFileRoot;
import common.util.Data;
import common.util.Res;
import common.util.pack.Background;
import common.util.pack.EffAnim;
import common.util.pack.NyCastle;
import common.util.pack.Soul;
import common.util.stage.CastleList.PackCasList;
import common.util.stage.CharaGroup;
import common.util.stage.Limit;
import common.util.stage.MapColc.DefMapColc;
import common.util.stage.MapColc.PackMapColc;
import common.util.stage.Music;
import common.util.stage.RandStage;
import common.util.unit.Combo;
import common.util.unit.EneRand;
import common.util.unit.Enemy;
import common.util.unit.Unit;
import common.util.unit.UnitLevel;

@JsonClass(read = RType.FILL, noTag = NoTag.LOAD)
public class PackData {

	public static class DefPack extends PackData {

		public VFileRoot<FileDesc> root = new VFileRoot<>(".");;

		public final Map<Integer, CharaGroup> cgmap = new HashMap<>();

		protected DefPack() {
		}

		public void load() {
			Res.readData();
			loadEnemies();
			loadUnits();

			Combo.readFile();
			PCoin.read();
			EffAnim.read();
			Background.read();
			NyCastle.read();

			loadSoul();

			DefMapColc.read();
			RandStage.read();

			loadCharaGroup();
			loadLimit();

			Orb.read();

			loadMusic();
		}

		private void loadCharaGroup() {
			Queue<String> qs = VFile.readLine("./org/data/Charagroup.csv");
			qs.poll();
			for (String str : qs) {
				String[] strs = str.split(",");
				int id = CommonStatic.parseIntN(strs[0]);
				int type = CommonStatic.parseIntN(strs[2]);
				@SuppressWarnings("unchecked")
				Identifier<Unit>[] units = new Identifier[strs.length - 3];
				for (int i = 3; i < strs.length; i++)
					units[i - 3] = Identifier.parseInt(CommonStatic.parseIntN(strs[i]), Unit.class);
				cgmap.put(id, new CharaGroup.DefCG(id, type, units));
			}
		}

		private void loadEnemies() {
			VFile.get("./org/enemy/").list().forEach(p -> enemies.add(new Enemy(p)));
			Queue<String> qs = VFile.readLine("./org/data/t_unit.csv");
			qs.poll();
			qs.poll();
			for (Enemy e : enemies.getList())
				((DataEnemy) e.de).fillData(qs.poll().split("//")[0].trim().split(","));
			qs = VFile.readLine("./org/data/enemy_dictionary_list.csv");
			for (String str : qs)
				enemies.get(Integer.parseInt(str.split(",")[0])).inDic = true;
		}

		private void loadLimit() {
			Queue<String> qs = VFile.readLine("./org/data/Stage_option.csv");
			qs.poll();
			for (String str : qs)
				new Limit.DefLimit(str.split(","));
		}

		private void loadMusic() {
			File dict = CommonStatic.ctx.getAssetFile("./music/");
			if (!dict.exists())
				return;
			File[] fs = dict.listFiles();
			for (File f : fs) {
				String str = f.getName();
				if (str.length() != 7)
					continue;
				if (!str.endsWith(".ogg"))
					continue;
				int id = CommonStatic.parseIntN(str.substring(0, 3));
				if (id == -1)
					continue;
				musics.set(id, new Music(Identifier.parseInt(id, Music.class), new FDFile(f)));
			}
		}

		private void loadSoul() {
			String pre = "./org/battle/soul/";
			String mid = "/battle_soul_";
			for (int i = 0; i < 13; i++)
				souls.add(new Soul(pre + Data.trio(i) + mid + Data.trio(i), i));
		}

		private void loadUnits() {
			VFile.get("./org/unit").list().forEach(p -> units.add(new Unit(p)));
			Queue<String> qs = VFile.readLine("./org/data/unitlevel.csv");
			List<Unit> lu = units.getList();
			FixIndexList<UnitLevel> l = unitLevels;
			for (Unit u : lu) {
				String[] strs = qs.poll().split(",");
				int[] lv = new int[20];
				for (int i = 0; i < 20; i++)
					lv[i] = Integer.parseInt(strs[i]);
				UnitLevel ul = new UnitLevel(lv);
				if (!l.contains(ul)) {
					ul.id = new Identifier<UnitLevel>(Identifier.DEF, UnitLevel.class, l.size());
					l.add(ul);
				}
				int ind = l.indexOf(ul);
				u.lv = l.get(ind);
				l.get(ind).units.add(u);
			}
			UnitLevel.def = l.get(2);
			qs = VFile.readLine("./org/data/unitbuy.csv");
			for (Unit u : lu) {
				String[] strs = qs.poll().split(",");
				u.rarity = Integer.parseInt(strs[13]);
				u.max = Integer.parseInt(strs[50]);
				u.maxp = Integer.parseInt(strs[51]);
				u.info.fillBuy(strs);
			}
		}

	}

	@JsonClass(noTag = NoTag.LOAD)
	public static class Identifier<T extends Indexable<?>> implements Comparable<Identifier<T>>, Cloneable {

		public static final String DEF = "_default";

		static final String STATIC_FIXER = "id_fixer";

		/**
		 * cls must be a class implementing Indexable. interfaces or other classes will
		 * go through fixer
		 */
		@SuppressWarnings("unchecked")
		public static <T extends Indexable<?>> Identifier<T> parseInt(int v, Class<? extends T> cls) {
			return parseIntRaw(v, cls);
		}

		@Deprecated
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static Identifier parseIntRaw(int v, Class<?> cls) {
			if (cls == null || cls.isInterface() || !Indexable.class.isAssignableFrom(cls))
				cls = UserProfile.getStatic(STATIC_FIXER, () -> new IdFixer(null)).parse(v, cls);
			String pack = v / 1000 == 0 ? DEF : Data.hex(v / 1000);
			int id = v % 1000;
			return new Identifier(pack, cls, id);
		}

		public Class<? extends T> cls;
		public String pack;
		public int id;

		@Deprecated
		public Identifier() {
			cls = null;
			pack = null;
			id = 0;
		}

		public Identifier(String pack, Class<? extends T> cls, int id) {
			this.cls = cls;
			this.pack = pack;
			this.id = id;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Identifier<T> clone() {
			return (Identifier<T>) Data.err(super::clone);
		}

		@Override
		public int compareTo(Identifier<T> o) {
			int val = pack.compareTo(o.pack);
			if (val != 0)
				return val;
			return Integer.compare(id, o.id);
		}

		public boolean equals(Identifier<T> o) {
			return pack.equals(o.pack) && id == o.id;
		}

		public T get() {
			return UserProfile.get(this);
		}

		@Override
		public String toString() {
			return pack + "/" + id;
		}

	}

	public static interface Indexable<T extends Indexable<?>> {

		public Identifier<T> getID();

	}

	@JsonClass(noTag = NoTag.LOAD)
	public static class PackDesc {
		public String BCU_VERSION;
		public String id;
		public String author;
		public String name;
		public String desc;
		public String time;
		public int version;
		@JsonField(generic = String.class)
		public ArrayList<String> dependency;

		@JCConstructor
		@Deprecated
		public PackDesc() {
		}

		public PackDesc(String id) {
			BCU_VERSION = AssetLoader.CORE_VER;
			this.id = id;
			this.dependency = new ArrayList<>();
		}
	}

	@JsonClass(read = RType.FILL)
	public static class UserPack extends PackData {

		@JsonField
		public final PackDesc desc;

		@JsonField(gen = GenType.FILL)
		public PackMapColc mc;

		@JsonField(gen = GenType.FILL)
		public PackCasList castles;

		public final Source source;

		public boolean editable;
		public boolean loaded = false;

		private JsonElement elem;

		/** for old reading method only */
		@Deprecated
		public UserPack(PackDesc desc, Source s) {
			this.desc = desc;
			source = s;
		}

		public UserPack(Source s, PackDesc desc, JsonElement elem) {
			this.desc = desc;
			this.elem = elem;
			source = s;
			editable = source instanceof Workspace;
			mc = new PackMapColc(this);
		}

		/** for generating new pack only */
		public UserPack(String id) {
			desc = new PackDesc(id);
			source = new Workspace(id);
			castles = new PackCasList(this);
			loaded = true;
		}

		public <T extends Indexable<?>> Identifier<T> getID(Class<T> cls, int id) {
			return new Identifier<T>(desc.id, cls, id);
		}

		public void loadMusics() {
			// FIXME
		}

		void load() throws Exception {
			JsonDecoder.inject(elem, UserPack.class, this);
			elem = null;
			loaded = true;
			loadMusics();
		}

	}

	public final FixIndexMap<Enemy> enemies = new FixIndexMap<>(Enemy.class);
	public final FixIndexMap<EneRand> randEnemies = new FixIndexMap<>(EneRand.class);
	public final FixIndexMap<Unit> units = new FixIndexMap<>(Unit.class);
	public final FixIndexMap<UnitLevel> unitLevels = new FixIndexMap<>(UnitLevel.class);
	public final FixIndexMap<Soul> souls = new FixIndexMap<>(Soul.class);
	public final FixIndexMap<Background> bgs = new FixIndexMap<>(Background.class);
	public final FixIndexMap<Music> musics = new FixIndexMap<>(Music.class);

}
