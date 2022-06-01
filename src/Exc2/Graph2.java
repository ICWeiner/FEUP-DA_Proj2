package Exc2;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Collections.reverse;

public class Graph2 {
  /** Number of nodes in the graph */
  int n;
  /** Number of relationships in the graph */
  int e;
  /** Vector of vectors of adjacencies */
  Vector<Vector<Integer>> adj;
  /** Matrix of capacities */
  int[][] cap;
  /** Matrix of time */
  int[][] time;
  /** List of lists with paths */
  List<List<Integer>> paths;
  /** List of flux */
  List<Integer> flux;
  /** List of Edges containing time */
  Set<Edge> CPMedges;
  
  /** @Constructor of Graph
   * @for to initialize all vectors for n nodes
   * @cap is initialized with n+1 since nodes start in 1 instead of 0
   * @time is initialized with n+1 since nodes start in 1 instead of 0  */
  public Graph2(int n, int e) {
    this.n = n;
    this.e = e;
    adj = new Vector<>();
    for (int i = 0; i <= n; i++) adj.add(new Vector<>());
    cap = new int[n + 1][n + 1];
    time = new int[n + 1][n + 1];
  }
  
  /** adjacency of node a to node b while also adding the capacity and time of the path */
  public void addLink(int a, int b, int c, int d) {
    adj.get(a).add(b);
    adj.get(b).add(a);
    cap[a][b] = c;
    time[a][b] = d;
  }
  
  /** @function BFS to find augmentation path returns flow value in that path
   * @Queue queue to be filled with nodes, initial insertion is with the starting node set to the max capacity
   * @while removes the first node from the queue and loops through the adjacent nodes only if the node hasn't been visited and the respective edge still has capacity to pass flow
   * @if when it enters the if clause it updates the father also updates the flux and checks if it has reached the end, if it hasn't then it adds the node to queue <br>
   * if it has reached the end return the new flow */
  int bfs(int s, int t, int[] parent) {
    for (int i = 1; i <= n; i++) parent[i] = -1;
    
    parent[s] = -2;
    Queue<NodeQ> q = new LinkedList<>();
    q.add(new NodeQ(s, Integer.MAX_VALUE));
    
    while (!q.isEmpty()) {
      int cur = q.peek().node;
      int flow = q.peek().flow;
      q.poll();
      
      for (int next : adj.get(cur)) {
        if (parent[next] == -1 && cap[cur][next] > 0) {
          parent[next] = cur;
          int new_flow = Math.min(flow, cap[cur][next]);
          if (next == t) return new_flow;
          q.add(new NodeQ(next, new_flow));
        }
      }
    }
    return 0;
  }
  
  /** @function Edmonds-Karp algorithm for maximum flow between s and t that returns maximum flow value (cap[][] gets residual graph)
   * @variables flow is the flow to be determined and array parent to rebuild the path also initialize the two different lists also the TreeSet
   * @flow is determined by calling the function bfs <br>
   * when the flow is returned from the bfs function we create a new path inside the list of lists <br>, and we add the flux to the flux list
   * then traverse the node cur until reaching the s node adding to the capacity array from prev to cur
   * the new flow and subtracting the flow from cur to prev creating the residual flow <br>
   * when the new flow is zero the while breaks and all the paths are sent to the different functions to create the different solutions for various exercises*/
  public void maxFlow( int t, int groupSize) {
    int s = 1;
    int flow = 0, i = 0;
    int[] parent = new int[n + 1];
    paths = new LinkedList<>();
    flux = new LinkedList<>();
    CPMedges = new TreeSet<>();
    
    while (true) {
      int new_flow = bfs(s, t, parent);
      if (new_flow == 0) {
        if (flow < groupSize) {
          System.out.println("Impossible to travel as a group");
          break;
        }
        System.out.println("\nFlow for the maximum group: " + flow);
        maxPath();
        System.out.println("\nFlow for the group of " + groupSize + ":");
        groupPath(groupSize);
        timeFLux();
        break;
      }
      
      paths.add(new LinkedList<>());
      flux.add(new_flow);
      flow += new_flow;
      int cur = t;
      while (cur != s) {
        paths.get(i).add(cur);
        int prev = parent[cur];
        cap[prev][cur] -= new_flow;
        cap[cur][prev] += new_flow;
        cur = prev;
      }
      i++;
    }
  }
  
  /** @function that determines all paths for max group by printing all possible paths inserted in the List of lists paths */
  private void maxPath() {
    int i=0;
    for (List<Integer> path : paths) {
      path.add(1);
      reverse(path);
      System.out.print("Flow on this path is " + flux.get(i) + ":");
      for (int a = 0; a < (path.size() - 1); a++)
        System.out.print(path.get(a) + " -> ");
      System.out.println(path.get(path.size() - 1));
      i++;
    }
  }
  
  /** @function that determines number of needed paths for the group amount specified
   * @variables max is the index of the list and maxInt is the value of flux at that position, size corresponds to the size of the group
   * @while inside the while the for looks for the max flow inside the list of flows when determined
   * it puts the i value that corresponds to the list inside the paths list then prints the list, the while breaks
   * when the size of the group minus the maxInt is equal or less the 0 <br>
   * inside this function we also add all the edges of the path tp a CPMedges list fpr another function relating the timeFlux */
  private void groupPath(int size) {
    int max = -1, maxInt = -1, s = 0;
    while(true){
      for(int i=0; i<flux.size() ;i++)
        if(flux.get(i) > maxInt){
          max = i;
          maxInt = flux.get(i);
        }

      System.out.print("Flow on this path is " + maxInt + " : ");
      for (int a=0; a<(paths.get(max).size()-1) ;a++)
        System.out.print(paths.get(max).get(a) + " -> ");
      System.out.println(paths.get(max).get(paths.get(max).size()-1));
      
      while(s < paths.get(max).size()-1){
        Edge e = new Edge(paths.get(max).get(s), paths.get(max).get(s+1), time[paths.get(max).get(s)][paths.get(max).get(s+1)]);
        CPMedges.add(e);
        s++;
      }

      if(size - maxInt <= 0) break;
      size -= maxInt;
      flux.remove(max);
      paths.remove(max);
      max = 0; maxInt = 0; s = 0;
    }
  }
  
  private void timeFLux(){
    int[] earliestStart = EarliestFlux();
    System.out.println("Earliest finish time is:" + earliestStart[n] );
    latestFlux(earliestStart);
  }

  private int[] EarliestFlux(){
    int[] ES = new int[n + 1];
    int[] prev = new int[n + 1];
    int[] entries = new int[n + 1]; // grau de entrada de cada no
    Stack<Integer> s = new Stack<>(); // stack que contem nos com grau de entrada de 0
    int minDur = -1;

    for (Edge e : CPMedges){
      entries[e.to] = entries[e.to] + 1;
    }

    for (Edge e : CPMedges){
      if (entries[e.start] == 0 && (!s.contains(e.start))) s.push(e.start);
    }

    while (!s.isEmpty()){
      int currNode = s.pop();
      //System.out.println("Current node: " + currNode);

      if (minDur < ES[currNode]) minDur = ES[currNode];

      for (Edge e : CPMedges){
        if(e.start == currNode){
          if (ES[e.to] < ES[currNode] + e.time){
            ES[e.to] = ES[currNode] + e.time;
            prev[e.to] = currNode;
          }
          entries[e.to] = entries[e.to] - 1;
          if(entries[e.to] == 0 ){
            s.push(e.to);
          }
        }
      }
    }

    ES[n] = minDur;

    return ES;
  }

  private void latestFlux(int ES[]){
    int[] LF = new int[n + 1];
    int[] exits = new int[n + 1]; // grau de saida de cada no
    Set<Edge> CPMedgesReverse = new TreeSet<>();
    Stack<Integer> s = new Stack<>();
    int totalWait = 0;

    for(int i = 1; i < LF.length; i++){
      LF[i] = ES[n];
    }

    for (Edge e : CPMedges){
      exits[e.start] = exits[e.start] + 1;//nao devia ser para e.start?
      CPMedgesReverse.add(new Edge(e.to , e.start,e.time));
    }

    for (Edge e : CPMedgesReverse){
      if (exits[e.start] == 0 && (!s.contains(e.start))) s.push(e.start);
      //System.out.println(e.start + "," + e.to);
    }

    while (!s.isEmpty()) {
      int currNode = s.pop();

      for (Edge e : CPMedgesReverse){

        if(e.start == currNode){
          if (LF[e.to] > LF[currNode] - e.time){
            LF[e.to] = LF[currNode] - e.time;

          }
          exits[e.to] = exits[e.to] - 1;
          if(exits[e.to] == 0 ){
            s.push(e.to);
          }
        }
      }
    }

    System.out.println();
    for(int i = 1 ; i < LF.length; i++){
      if(ES[i]<LF[i] && ES[i]!=0){
        int currWait = LF[i] - ES[i];
        System.out.println("Tempo de espera livre:" + currWait + " no nó:" + i);
        totalWait+=currWait;
      }
      //System.out.print(LF[i] + " ");

    }
    System.out.println("Tempo total livre:" + totalWait);
  }

}
