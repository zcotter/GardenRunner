package com.zachcotter.gardenrunner;

public interface JSONSerializable {
  public String serialize();
  public void deserialize(String json);
}
