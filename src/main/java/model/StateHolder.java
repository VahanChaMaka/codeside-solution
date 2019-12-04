package model;

import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StateHolder<T> {

    private LinkedList<T> states;
    private int size;

    public StateHolder(int size){
        this.size = size;
        states = new LinkedList<>();
    }

    public void put(T newState){
        states.addFirst(newState);
        if(states.size() > size){
            states.removeLast();
        }
    }

    public T get(int i){
        return states.get(i);
    }


    public static void main(String[] args) {
        StateHolder<Integer> s = new StateHolder<>(3);
        s.put(1);
        s.put(2);
        s.put(3);

        System.out.println(s.states);
        //[3, 2, 1]

        s.put(4);
        System.out.println(s.states);
        //[4, 3, 2]

    }
}
