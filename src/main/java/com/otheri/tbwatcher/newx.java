package com.otheri.tbwatcher;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class newx {

	public static void main(String[] args) {
		try {
			// zhuanjia();

			zhuanjiadetail("http://www.idp.cn/HTML/nanjing/zhuanjiatuandui/2011/1206/2625.html");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static String selectFirstElement2String(Document doc, String query)
			throws Exception {
		Elements es = doc.select(query);
		if (es.size() > 0) {
			return Jsoup.clean(doc.select(query).first().html(),
					Whitelist.basic());
		} else {
			throw new Exception("query empty @ " + query);
		}
	}

	private static void zhuanjia() throws Exception {
		String list1 = "http://www.idp.cn/HTML/liuxuezhuanjia/list_522_1.html";
		String list2 = "http://www.idp.cn/HTML/liuxuezhuanjia/list_522_2.html";

		zhuanjialist(list1);
		zhuanjialist(list2);
	}

	private static void zhuanjialist(String listurl) throws Exception {
		Document doc = Jsoup.connect(listurl).get();
		Elements guwens = doc.select("div.guwen_nei");
		ArrayList<String> urls = new ArrayList<String>();
		for (Element e : guwens) {
			Element el = e.select("span.guwen_name a").get(0);
			// String str = Jsoup.clean(el.text(), Whitelist.basic());
			// System.out.println(str);
			String url = "http://www.idp.cn" + el.attr("href");
			urls.add(url);
		}
		for (String str : urls) {
			zhuanjiadetail(str);
		}
		System.out.println();
	}

	private static void zhuanjiadetail(String detailurl) throws Exception {
		Document doc = Jsoup.connect(detailurl).get();
		Element neirong = doc.select("div.guwen_neirong_xx").first();

		List<TextNode> nodes = neirong.textNodes();

		ArrayList<String> strs = new ArrayList<String>();
		for (TextNode node : nodes) {
			String txt = node.text().trim();
			if (!txt.equals("")) {
				strs.add(node.text().trim());
//				System.out.println(node.text().trim());
			}

		}

		String name = strs.get(0);
		String title = strs.get(1);
		String office = strs.get(2);
		String[] tags = strs.get(3).split(",");

		System.out.println(name);
		System.out.println(title);
		System.out.println(office);

		for (String tag : tags) {
			System.out.print(tag);
			System.out.print(" | ");
		}

		System.out.println();
	}
}
