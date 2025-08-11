package com.hbm.qmaw;

import java.util.HashMap;
import java.util.Stack;

import net.minecraft.item.ItemStack;

public class QuickManualAndWiki {

	public String name;
	public ItemStack icon;

	public HashMap<String, String> title = new HashMap<>();
	public HashMap<String, String> contents = new HashMap<>();

	public QuickManualAndWiki(String name) {
		this.name = name;
	}

	public QuickManualAndWiki setIcon(ItemStack stack) {
		this.icon = stack;
		return this;
	}

	public QuickManualAndWiki addTitle(String lang, String title) {
		this.title.put(lang, title);
		return this;
	}

	public QuickManualAndWiki addLang(String lang, String contents) {
		this.contents.put(lang, contents);
		return this;
	}

	private static Stack<QuickManualAndWiki> history = new Stack<>();
	public static QuickManualAndWiki currentPage;

	public static void clearHistory() {
		history = new Stack<>();
	}

	public static void pushHistory(QuickManualAndWiki page) {
		history.add(page);
	}

	public static QuickManualAndWiki popHistory() {
		if(history.empty()) return null;
		return history.pop();
	}

	public static QuickManualAndWiki peekHistory() {
		if(history.empty()) return null;
		return history.peek();
	}

}
