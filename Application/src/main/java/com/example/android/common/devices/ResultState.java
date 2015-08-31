package com.example.android.common.devices;

import android.graphics.Color;

public class ResultState implements State {

    private State mResultState;
    private String mText = "BLANK";

    public ResultState() {
        // default constructor is limbo state
        this.setState(new ResultStateLimbo());
        this.setText("INITIAL STATE IS LIMBO.");
    }

    public void setState(State state) {
        this.mResultState = state;
    }

    public State getState() {
        return this.mResultState;
    }

    @Override
    public void doAction() {
        this.mResultState.doAction();
    }

    public void showState() {
        String s = this.getText();
        s += "\nvalue = " + this.getValue() + ", ";
        s += "color: " + this.getColor() + "";
        System.out.println(s);
    }

    public int getValue() { return this.mResultState.getValue(); }

    public String getWord() { return this.mResultState.getWord(); }

    public int getColor() { return this.mResultState.getColor(); }

    public String getText() { return this.getWord() + ": " + mText; }

    public void setText(String s) { mText = s; }

    public static void main(String[] args) {

        ResultState resultState = new ResultState();
        resultState.showState();

        resultState.setState(new ResultStateGood());
        resultState.setText("It's all good!");
        resultState.showState();
        //resultState.doAction();
        //System.out.println(resultState.getText());

        resultState.setState(new ResultStateLimbo());
        resultState.setText("Limbo is a great physics puzzle game.");
        resultState.showState();
        //resultState.doAction();
        //System.out.println(resultState.getText());

        resultState.setState(new ResultStateBad());
        resultState.setText("Bad is an undesirable state.");
        resultState.showState();
        //resultState.doAction();
        //System.out.println(resultState.getText());

    }

}

interface State {
    void doAction();
    String getWord();
    int getValue();
    int getColor();
}

class ResultStateGood implements State {

    @Override
    public void doAction() {
        System.out.println("Result state is doing good action.");
    }

    @Override
    public String getWord() { return "GOOD"; }

    @Override
    public int getValue() { return 1; }

    @Override
    public int getColor() {
        return Color.GREEN;
    }

}

class ResultStateBad implements State {

    @Override
    public void doAction() {
        System.out.println("Result state is doing bad action.");
    }

    @Override
    public String getWord() { return "BAD"; }

    @Override
    public int getValue() { return -1; }

    @Override
    public int getColor() { return Color.RED; }

}

class ResultStateLimbo implements State {

    @Override
    public void doAction() {
        System.out.println("Result state is doing some limbo action.");
    }

    @Override
    public String getWord() { return "LIMBO"; }

    @Override
    public int getValue() { return 0; }

    @Override
    public int getColor() { return Color.YELLOW; }

}
