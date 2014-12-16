package com.zachcotter.gardenrunner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import edu.neu.madcourse.zachcotter.garden.Vegetable.Species;

public class Store extends Activity implements OnClickListener {

  public static final String MONEY_KEY = "money";
  public static final int MONEY_PER_METER = 1;

  private Set<Item> itemsForSale;

  private TextView moneyView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.store);
    moneyView = (TextView) findViewById(R.id.money_view);
    findViewById(R.id.exit_store).setOnClickListener(this);
    refresh();
    Garden.notifyOfAnyPlantDeaths(this);
  }

  private void refresh() {
    refreshUnlockables();
    moneyView.setText("You have $" + getMoney(this));
    moneyView.invalidate();
    populateStore();
  }

  private void populateStore() {
    LinearLayout storeContent = (LinearLayout) findViewById(R.id.store_content);
    storeContent.removeAllViews();
    for(Item i : itemsForSale) {
      ItemView view = new ItemView(this,
                                   i);
      storeContent.addView(view);
    }
  }

  private void refreshUnlockables() {
    itemsForSale = new HashSet<Item>();
    Set<String> unavailable = ActiveRunMapActivity.getUnavailableSpecies(this);
    for(String u : unavailable) {
      itemsForSale.add(new SinglePurchaseSpecies(Species.valueOf(u).getCost(),
                                                 u));
    }
  }

  public static void addMoney(int amount,
                              Context context) {
    changeMoney(amount,
                context);
  }

  public static void removeMoney(int amount,
                                 Context context) {
    changeMoney(-1 * amount,
                context);
  }

  private static void changeMoney(int difference,
                                  Context context) {
    SharedPreferences prefs = context.getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                           Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    int money = getMoney(context) + difference;
    editor.putInt(MONEY_KEY,
                  money);
    editor.commit();
  }

  public static int getMoney(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                           Context.MODE_PRIVATE);
    return prefs.getInt(MONEY_KEY,
                        0);
  }

  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.exit_store:
        startActivity(new Intent(this,
                                 GardenMenu.class));
        finish();
        return;
    }
  }

  private class Item {
    private int cost;
    private String name;

    private Item(int cost,
                 String name) {
      this.cost = cost;
      this.name = name;
    }

    public int getCost() {
      return cost;
    }

    public String getName() {
      return name;
    }

    public void buy(Context context) {
      if(this.getCost() <= getMoney(context)) {
        removeMoney(this.getCost(),
                    context);
        incrementQuantity(context);
      }
    }

    private void incrementQuantity(Context context) {
      SharedPreferences prefs = context.getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                             Context.MODE_PRIVATE);
      int currentAmount = getQuantity(context);
      Editor editor = prefs.edit();
      editor.putInt(getName(),
                    currentAmount + 1);
      editor.commit();
    }

    private int getQuantity(Context context) {
      SharedPreferences prefs = context.getSharedPreferences(Garden.GARDEN_PREFERENCES_KEY,
                                                             Context.MODE_PRIVATE);
      return prefs.getInt(getName(),
                          0);
    }

    public String buttonText(){
      return "BUY";
    }
  }

  private class SinglePurchaseSpecies extends Item {

    private SinglePurchaseSpecies(int cost,
                                  String name) {
      super(cost,
            name);
    }

    @Override
    public void buy(Context context) {
      ActiveRunMapActivity.addAvailableSpecies(getApplicationContext(),
                                               Species.valueOf(this.getName()));
      super.buy(context);
    }

    public String buttonText(){
      return "UNLOCK";
    }
  }

  public class ItemView extends LinearLayout implements OnClickListener {

    private Item item;

    public ItemView(Context context,
                    Item item) {
      super(context);
      this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                                            LayoutParams.WRAP_CONTENT));
      this.item = item;
      TextView itemLabel = new TextView(getContext());
      itemLabel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                                                 LayoutParams.MATCH_PARENT));
      itemLabel.setText(item.getName() + ": $" + item.getCost());
      this.addView(itemLabel);

      Button buyButton = new Button(getContext());
      buyButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                                                 LayoutParams.MATCH_PARENT));
      buyButton.setText(item.buttonText());
      buyButton.setEnabled(getMoney(getContext()) >= item.getCost());
      buyButton.setOnClickListener(this);
      buyButton.setId(item.getName().hashCode());
      this.addView(buyButton);
    }

    @Override
    public void onClick(View view) {
      final int id = item.getName().hashCode();
      if(id == view.getId()){
        item.buy(getContext());
        refresh();
      }
    }
  }
}
