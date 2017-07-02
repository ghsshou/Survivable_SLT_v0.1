package edu.bupt.tao.traffic_SSLT.basic_model;

/**
 * Created by Gao Tao on 2017/6/8.
 */
public class Multicast_Request {
    public int id;
    public int req_service;
    public int[] users;
    public double capacity;
    public long due_time;

    public Multicast_Request(int id, int req_service, int[] users, double capacity) {
        this.id = id;
        this.req_service = req_service;
        this.users = users;
        this.capacity = capacity;
    }
    public Multicast_Request(int id, int req_service, int[] users, double capacity, long due_time) {
        this.id = id;
        this.req_service = req_service;
        this.users = users;
        this.capacity = capacity;
        this.due_time = due_time;
    }
}
