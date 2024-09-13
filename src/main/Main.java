package main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class Main {
	public static final int maxKind = 32;
	public static final HashMap<Integer, Integer> price32 = new HashMap<>();
	static {
//		price32.put(5, 6620);
//		price32.put(6, 7130);
//		price32.put(7, 7540);
//		price32.put(8, 7940);
//		price32.put(9, 8350);
		price32.put(10, 8760);
		price32.put(20, 12330);
		price32.put(30, 16160);
		price32.put(40, 19990);
		price32.put(50, 23820);
		price32.put(60, 27650);
		price32.put(70, 31480);
		price32.put(80, 35310);
		price32.put(90, 39140);
		price32.put(100, 42970);
		price32.put(120, 49100);
		price32.put(140, 55230);
		price32.put(160, 61360);
		price32.put(180, 67490);
		price32.put(200, 73620);
	}
	public static final List<Integer> lotList = price32.keySet().stream().sorted().toList();

	private static final Order[] tasumachi = { new Order(13, 4, Product.tasu1), new Order(3, 8, Product.tasu2) };
	private static final Order[] drifters = { new Order(20, 1, Product.drift) };
	private static final Order[] founder = { new Order(11, 1, Product.founder1), new Order(17, 2, Product.founder2),
			new Order(2, 4, Product.founder3) };

	public static final int tasuSize = Stream.of(tasumachi).mapToInt(Order::size).sum();
	public static final int driftSize = Stream.of(drifters).mapToInt(Order::size).sum();
	public static final int founderSize = Stream.of(founder).mapToInt(Order::size).sum();

	public static void main(String[] args) {
		int tasuMax = 30, driftMax = 30, founderMax = 30;

		ArrayList<Info> rank = new ArrayList<>();

		for (int i = 10; i <= tasuMax; i += 5) {
			for (int j = 10; j <= driftMax; j += 5) {
				for (int k = 10; k <= founderMax; k += 5) {
					ArrayList<Order> order = new ArrayList<>();
					for (Order o : tasumachi)
						order.add(new Order(o.kind(), o.lot() * i, o.product()));
					for (Order o : drifters)
						order.add(new Order(o.kind(), o.lot() * j, o.product()));
					for (Order o : founder)
						order.add(new Order(o.kind(), o.lot() * k, o.product()));
					rank.add(new Info(search(order, Integer.MAX_VALUE, 0, Integer.MAX_VALUE), i, j, k));
					rank.getLast().printInfo();
				}
			}
		}
		System.out.println("\n ~~ answer ~~ \n");
		for (Info info : rank.stream().sorted(Comparator.comparing(Info::getCostPerformancePerCard)).toList())
			info.printInfo();
	}

	private static int stage = 0;

	private static ArrayList<ArrayList<Order>> search(ArrayList<Order> order, int nowLot, int nowPrice, int minPrice) {
		if (order.isEmpty())
			return new ArrayList<>();

		ArrayList<ArrayList<Order>> ret = null;

		int minLot = order.stream().mapToInt(Order::size).sum() / Main.maxKind;
		for (int lot : lotList) {
			if (minLot <= lot) {
				if (nowPrice + price32.get(lot) >= minPrice)
					return null;
				break;
			}
		}

		stage++;
		for (int lot : getKeyListByLot(nowLot)) {
			if (nowPrice + price32.get(lot) > minPrice)
				continue;
//			if (stage == 1)
//				System.out.println("now + " + lot + " : " + minPrice);
			for (SearchData data : fill(new ArrayList<>(order), new ArrayList<>(), lot)) {
				ArrayList<ArrayList<Order>> tmpRet = search(data.order(), lot, nowPrice + price32.get(lot), minPrice);
				if (tmpRet != null) {
					ret = tmpRet;
					ret.add(new ArrayList<Order>(data.allocation()));
					minPrice = nowPrice + Info.calcPrice(ret.stream().map(e -> e.getFirst().lot()).toList());
				}
			}
		}
		stage--;
		return ret;
	}

	private static ArrayList<SearchData> fill(ArrayList<Order> order, ArrayList<Order> allocation, int lot) {
		ArrayList<SearchData> ret = new ArrayList<>();
		ArrayList<Order> willAddList = new ArrayList<>();
		int remain = Main.maxKind - allocation.stream().mapToInt(e -> e.kind()).sum();

		order.sort(Comparator.comparing(Order::lot, Comparator.reverseOrder()).thenComparing(Order::kind));
		if (order.isEmpty() || remain == 0) {
			ret.add(new SearchData(order, allocation));
			return ret;
		}

		boolean isFirst = true;
		int removeCount = 0;
		while (!order.isEmpty()) {
			Order o = order.removeFirst();
			Order willAdd = o;

			if (!isFirst && o.lot() < lot)
				break;

			if (o.kind() > remain) {
				Order tmpO = new Order(o.kind() - remain, o.lot(), o.product());
				order.addLast(tmpO);
				o = new Order(remain, o.lot(), o.product());
				removeCount++;
			}
			if (o.lot() > lot) {
				Order tmpO = new Order(o.kind(), o.lot() - lot, o.product());
				order.addLast(tmpO);
				removeCount++;
			}
			allocation.addLast(new Order(o.kind(), lot, o.product()));
			ArrayList<SearchData> tmpRet = fill(new ArrayList<>(order), new ArrayList<>(allocation), lot);
			if (!isFirst && tmpRet.getFirst().remain() != 0)
				break;
			tmpRet.forEach(e -> e.order().addAll(0, willAddList));
			ret.addAll(tmpRet);
			allocation.removeLast();
			while (removeCount > 0) {
				removeCount--;
				order.removeLast();
			}
			willAddList.add(willAdd);
			isFirst = false;
		}
		return ret;
	}

	private static LinkedList<Integer> getKeyListByLot(int lot) {
		LinkedList<Integer> ret = new LinkedList<>();
		for (int key : lotList) {
			ret.addFirst(key);
			if (key >= lot)
				return ret;
		}
		return ret;
	}

}

record SearchData(ArrayList<Order> order, ArrayList<Order> allocation) {
	public int remain() {
		return Main.maxKind - allocation.stream().mapToInt(e -> e.kind()).sum();
	}
}

enum Product {
	tasu1, tasu2, drift, founder1, founder2, founder3,
	;
}

record Order(int kind, int lot, Product product) {
	public int size() {
		return kind * lot;
	}
}

record Info(ArrayList<ArrayList<Order>> separateList, int tasuLot, int driftLot, int founderLot) {
	public static int calcPrice(List<Integer> list) {
		return list.stream().mapToInt(e -> Main.price32.get(e)).sum();
	}

	public double getCostPerformanceAverage() {
		return (double) calcPrice(separateList.stream().map(e -> e.getFirst().lot()).toList())
				/ (tasuLot + driftLot + founderLot);
	}

	public double getCostPerformancePerCard() {
		return (double) calcPrice(separateList.stream().map(e -> e.getFirst().lot()).toList())
				/ (tasuLot * Main.tasuSize + driftLot * Main.driftSize + founderLot * Main.founderSize);
	}

	public void printInfo() {
		System.out.println(
				"price: " + calcPrice(separateList.stream().map(e -> e.getFirst().lot()).toList()));
		System.out.print("lot: ");
		separateList.forEach(e -> System.out.print(e.getFirst().lot() + ", "));
		System.out.println();
		System.out.println("allocation: ");
		separateList.forEach(e -> {
			System.out.print("(");
			e.forEach(e2 -> System.out.print("" + e2.product() + ": " + e2.kind() + ", "));
			System.out.println(")");
		});
		System.out.println("tasu: " + tasuLot + ", drift: " + driftLot + ", founder: " + founderLot);
	}
}

/*
 * ・たす街。
1ゲームに
13種類のカードが各4枚
3種類のカードが各8枚
合計76枚　(25ロット制作予定)

・Drifters of Galaxy
20種類のカードが各1枚
合計20枚　(30ロット制作予定だが、バッファなのでカード枚数含め増減対応可能。)

・教祖王
11種類のカードが各1枚
17種類のカードが各2枚
2種類のカードが各4枚
合計53枚(15ロット制作予定)
全ゲームのカード合計56種　3,295枚
*/