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
		price32.put(5, 6620);
		price32.put(6, 7130);
		price32.put(7, 7540);
		price32.put(8, 7940);
		price32.put(9, 8350);
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
		/*
		int tasuMax = 25, driftMax = 20, founderMax = 20;
		
		ArrayList<Info> rank = new ArrayList<>();
		
		for (int i = 10; i <= tasuMax; i += 5) {
			for (int j = 10; j <= driftMax; j += 10) {
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
		*/
		System.out.println("\n ~~ answer ~~ \n");
		ArrayList<Order> order = new ArrayList<>();
		for (Order o : tasumachi)
			order.add(new Order(o.kind(), o.lot() * 25, o.product()));
		for (Order o : drifters)
			order.add(new Order(o.kind(), o.lot() * 20, o.product()));
		for (Order o : founder)
			order.add(new Order(o.kind(), o.lot() * 15, o.product()));

		searchAllByLot(order, new int[] { 50, 30, 20 }, 0).stream()
				.sorted(Comparator.comparing(e -> ((ArrayList<OrderList>) e).getFirst().remainKind(), Comparator.reverseOrder())
						.thenComparing(e -> ((ArrayList<OrderList>) e).stream().mapToInt(OrderList::size).sum()))
				.limit(30).forEach(e -> {
					new Info(e, 25, 20, 15).printAllocation();
					System.out.println();
				});
		/*
		for (Info info : rank.stream().sorted(Comparator.comparing(Info::getCostPerformancePerCard)).toList())
			info.printInfo();
			*/
	}

	private static ArrayList<ArrayList<OrderList>> searchAllByLot(ArrayList<Order> order, int[] lotList, int index) {
		ArrayList<ArrayList<OrderList>> ret = null;
		int lot = lotList[index];

		if (index == lotList.length - 1) {
			ret = new ArrayList<>();
			SearchData tmp = fillOne(new ArrayList<>(order), new OrderList(lot));
			if (tmp == null)
				return null;
			ArrayList<OrderList> tmpRet = new ArrayList<>();
			tmpRet.add(tmp.allocation());
			ret.add(tmpRet);
			return ret;
		}

		ArrayList<SearchData> allocationList = hyperFill(new ArrayList<>(order), new OrderList(lot));
		System.out.println(index + ", " + allocationList.size());
		allocationList.sort(index == 0 ? Comparator.comparing(SearchData::allocateRate, Comparator.reverseOrder()) : Comparator.comparing(SearchData::remainOrder));
		int allowRate = allocationList.getFirst().remainOrder();
		for (SearchData data : allocationList.stream().filter(e -> e.remainOrder() == allowRate).limit(50).toList()) {
			ArrayList<ArrayList<OrderList>> tmpRet = searchAllByLot(data.order(), lotList, index + 1);
			if (tmpRet != null) {
				tmpRet.forEach(e -> e.add(new OrderList(data.allocation())));
				if (ret != null)
					ret.addAll(tmpRet);
				else
					ret = tmpRet;
			}
		}
		return ret;
	}

	private static int stage = 0;

	private static ArrayList<ArrayList<OrderList>> searchAll(ArrayList<Order> order, int nowLot, int nowPrice,
			int minPrice) {
		if (order.isEmpty()) {
			ArrayList<ArrayList<OrderList>> ret = new ArrayList<>();
			ret.add(new ArrayList<>());
			return ret;
		}
		if (stage == 4)
			return null;

		ArrayList<ArrayList<OrderList>> ret = null;

		int minLot = (order.stream().mapToInt(Order::size).sum() - 1) / Main.maxKind + 1;
		for (int lot : lotList) {
			if (minLot <= lot) {
				if (nowPrice + price32.get(lot) > minPrice)
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
			ArrayList<SearchData> allocationList = fill(new ArrayList<>(order), new OrderList(lot));
			allocationList.sort(Comparator.comparing(SearchData::allocateRate, Comparator.reverseOrder()));
			double allowRate = allocationList.getFirst().allocateRate() - 0.25;
			for (SearchData data : allocationList.stream().filter(e -> e.allocateRate() >= allowRate).toList()) {
				ArrayList<ArrayList<OrderList>> tmpRet = searchAll(data.order(), lot, nowPrice + price32.get(lot),
						minPrice);
				if (tmpRet != null) {
					tmpRet.forEach(e -> e.add(new OrderList(data.allocation())));
					if (ret != null
							&& Info.calcPrice(tmpRet.getFirst().stream().map(OrderList::getLot).toList()) == minPrice)
						ret.addAll(tmpRet);
					else {
						ret = tmpRet;
						minPrice = nowPrice + Info.calcPrice(ret.getFirst().stream().map(OrderList::getLot).toList());
					}
				}
			}
		}
		stage--;
		return ret;
	}

	private static ArrayList<OrderList> search(ArrayList<Order> order, int nowLot, int nowPrice, int minPrice) {
		if (order.isEmpty())
			return new ArrayList<>();
		if (stage == 6)
			return null;

		ArrayList<OrderList> ret = null;

		int minLot = (order.stream().mapToInt(Order::size).sum() - 1) / Main.maxKind + 1;
		for (int lot : lotList) {
			if (minLot <= lot) {
				if (nowPrice + price32.get(lot) >= minPrice)
					return null;
				break;
			}
		}

		stage++;
		for (int lot : getKeyListByLot(nowLot)) {
			if (nowPrice + price32.get(lot) >= minPrice)
				continue;
			//			if (stage == 1)
			//				System.out.println("now + " + lot + " : " + minPrice);
			ArrayList<SearchData> allocationList = fill(new ArrayList<>(order), new OrderList(lot));
			allocationList.sort(Comparator.comparing(SearchData::allocateRate, Comparator.reverseOrder()));
			double allowRate = allocationList.getFirst().allocateRate() - 0.25;
			for (SearchData data : allocationList.stream().filter(e -> e.allocateRate() >= allowRate).toList()) {
				ArrayList<OrderList> tmpRet = search(data.order(), lot, nowPrice + price32.get(lot), minPrice);
				if (tmpRet != null) {
					ret = tmpRet;
					ret.add(new OrderList(data.allocation()));
					minPrice = nowPrice + Info.calcPrice(ret.stream().map(OrderList::getLot).toList());
				}
			}
		}
		stage--;
		return ret;
	}

	private static SearchData fillOne(ArrayList<Order> order, OrderList allocation) {
		int remain = Main.maxKind - allocation.stream().mapToInt(Order::kind).sum();
		int lot = allocation.getLot();

		if (order.isEmpty())
			return new SearchData(order, allocation);

		Order o = order.removeFirst();

		if (o.kind() > remain)
			return null;
		if (o.lot() > lot)
			order.add(new Order(o.kind(), o.lot() - lot, o.product()));
		allocation.addLast(new Order(o.kind(), Math.min(o.lot(), lot), o.product()));
		return fillOne(order, allocation);
	}

	private static int stage2 = 0;
	private static ArrayList<SearchData> hyperFill(ArrayList<Order> order, OrderList allocation) {
		ArrayList<SearchData> ret = new ArrayList<>();
		ArrayList<Order> willAddList = new ArrayList<>();
		int remain = Main.maxKind - allocation.stream().mapToInt(Order::kind).sum();
		int lot = allocation.getLot();

		if (order.isEmpty() || remain == 0) {
			ret.add(new SearchData(order, allocation));
			return ret;
		}
		if (stage2 == 7)
			return ret;
		stage2 ++;
		while (!order.isEmpty()) {
			Order o = order.removeFirst();

			for (int i = 1; i <= Math.min(o.kind(), remain); i++) {
				ArrayList<Order> tmpOrder = new ArrayList<>(order);
				OrderList tmpAllocation = new OrderList(allocation);
				if (o.lot() > lot)
					tmpOrder.add(new Order(i, o.lot() - lot, o.product()));
				tmpAllocation.addLast(new Order(i, Math.min(o.lot(), lot), o.product()));
				ArrayList<SearchData> tmpRet = hyperFill(tmpOrder, tmpAllocation);
				if (i < o.kind())
					willAddList.add(new Order(o.kind() - i, o.lot(), o.product()));
				tmpRet.forEach(e -> e.order().addAll(0, willAddList));
				if (i < o.kind())
					willAddList.removeLast();
				ret.addAll(tmpRet);
			}
			willAddList.add(o);
		}
		stage2 --;
		return ret;
	}

	private static ArrayList<SearchData> fill(ArrayList<Order> order, OrderList allocation) {
		ArrayList<SearchData> ret = new ArrayList<>();
		ArrayList<Order> willAddList = new ArrayList<>();
		int remain = Main.maxKind - allocation.stream().mapToInt(Order::kind).sum();
		int lot = allocation.getLot();

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
			allocation.addLast(o.clone());
			ArrayList<SearchData> tmpRet = fill(new ArrayList<>(order), new OrderList(allocation));
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

record SearchData(ArrayList<Order> order, OrderList allocation) {
	public int remainOrder() {
		return order.stream().mapToInt(e -> e.kind()).sum();
	}

	public int remain() {
		return Main.maxKind - allocation.stream().mapToInt(e -> e.kind()).sum();
	}

	public double allocateRate() {
		return allocation.allocateRate();
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

	@Override
	public Order clone() {
		return new Order(kind, lot, product);
	}
}

class OrderList extends ArrayList<Order> {
	private final int lot;

	public OrderList(int lot) {
		this.lot = lot;
	}

	public OrderList(OrderList src) {
		super(src);
		this.lot = src.getLot();
	}

	public int getLot() {
		return lot;
	}

	public double allocateRate() {
		return (double) this.stream().mapToInt(Order::size).sum() / (Main.maxKind * this.getLot());
	}

	public int remainKind() {
		return Main.maxKind - this.stream().mapToInt(Order::kind).sum();
	}
}

record Info(ArrayList<OrderList> separateList, int tasuLot, int driftLot, int founderLot) {
	public static int calcPrice(List<Integer> list) {
		return list.stream().mapToInt(e -> Main.price32.get(e)).sum();
	}

	public double getCostPerformanceAverage() {
		return (double) calcPrice(separateList.stream().map(OrderList::getLot).toList())
				/ (tasuLot + driftLot + founderLot);
	}

	public double getCostPerformancePerCard() {
		return (double) calcPrice(separateList.stream().map(OrderList::getLot).toList())
				/ (tasuLot * Main.tasuSize + driftLot * Main.driftSize + founderLot * Main.founderSize);
	}

	public void printInfo() {
		System.out.println(
				"price: " + calcPrice(separateList.stream().map(OrderList::getLot).toList()));
		System.out.print("lot: ");
		separateList.forEach(e -> System.out.print(e.getLot() + ", "));
		System.out.println();
		System.out.println("allocation: ");
		separateList.forEach(e -> {
			System.out.print("(");
			e.forEach(e2 -> System.out.print("" + e2.product() + ": " + e2.kind() + ", "));
			System.out.println(")");
		});
		System.out.println("tasu: " + tasuLot + ", drift: " + driftLot + ", founder: " + founderLot);
	}

	public void printAllocation() {
		separateList.forEach(e -> {
			System.out.print("(");
			e.forEach(e2 -> System.out.print("" + e2.product() + ": " + e2.kind() + ", "));
			System.out.println(")");
		});
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