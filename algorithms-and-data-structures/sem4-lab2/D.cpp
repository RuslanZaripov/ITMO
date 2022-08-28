#include <iostream>
#include <vector>
#include <sstream>
#include <set>

const long long INF = 1e9;
int source = 0;
int sink = -1;

enum Points {
    THREE = 3,
    TWO = 2,
    ONE = 1,
    ZERO = 0
};

struct Edge
{
    int from{}, to{};
    long long maxFlow{}, flow{};
    Edge* backward = nullptr;

    Edge() = default;

    Edge(int from, int to, long long flow, long long maxFlow)
        : from(from), to(to), flow(flow), maxFlow(maxFlow) {}

    ~Edge() { delete backward; }
};

std::vector<std::vector<Edge*>> g;
std::vector<std::set<int>> match;
std::vector<std::vector<char>> table;
std::vector<bool> visited;
std::vector<int> rating;

long long dfs(int node, long long minFlow)
{
    if (node == sink) return minFlow;

    visited[node] = true;
    for (Edge* edge : g[node])
    {
        if (!visited[edge->to] && edge->flow < edge->maxFlow)
        {
            long long delta = dfs(edge->to, std::min(minFlow, edge->maxFlow - edge->flow));
            if (delta > 0)
            {
                edge->flow += delta;
                edge->backward->flow -= delta;
                return delta;
            }
        }
    }
    return 0;
}

void addEdge(int u, int v, int maxFlow) {
    Edge* edge = new Edge(u, v, 0, maxFlow);
    Edge* reversedEdge = new Edge(v, u, 0, 0);
    edge->backward = reversedEdge;
    reversedEdge->backward = edge;
    g[u].push_back(edge);
    g[v].push_back(reversedEdge);
}

int getCompetitorCount(int team) {
    int competitorCount = 0;
    for (int competitor : match[team]) {
        if (competitor < team) {
            competitorCount += 1;
        }
    }
    return competitorCount;
}

std::pair<char, char> getResult(long long points) {
    switch (points) {
    case Points::THREE: return { 'L', 'W' };
    case Points::TWO: return { 'l', 'w' };
    case Points::ONE: return { 'w', 'l' };
    case Points::ZERO: return { 'W', 'L' };
    default: return { '#', '#' };
    }
}

int getPoints(char symbol) {
    switch (symbol) {
    case 'W': return Points::THREE;
    case 'w': return Points::TWO;
    case 'l': return Points::ONE;
    case 'L': return Points::ZERO;
    default: return 0;
    }
}

int main()
{
    std::ios::sync_with_stdio(false);
    std::cin.tie(nullptr);

    int N;
    std::cin >> N;
    sink = N + 1;

    g.resize(N + 2);
    rating.assign(N, 0);
    table.resize(N, std::vector<char>(N));
    match.resize(N);

    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            char symbol;
            std::cin >> symbol;
            table[i][j] = symbol;
            if ('.' == symbol) {
                match[i].insert(j);
            }
            else {
                rating[i] += getPoints(symbol);
            }
        }
    }

    for (int& i : rating) {
        int points;
        std::cin >> points;
        i = std::max(0, points - i);
    }

    for (int team = 0; team < N; team++) {
        addEdge(source, team + 1, Points::THREE * getCompetitorCount(team));
        addEdge(team + 1, sink, rating[team]);
        for (int competitor : match[team]) {
            if (competitor < team) {
                addEdge(team + 1, competitor + 1, Points::THREE);
            }
        }
    }

    do
    {
        visited.assign(N + 2, false);
    } while (dfs(source, INF));

    for (int team = 1; team <= N; team++) {
        for (Edge* edge : g[team]) {
            int competitor = edge->to;
            if (competitor != source && competitor != sink && competitor < team) {
                std::pair<char, char> result = getResult(edge->flow);
                table[team - 1][competitor - 1] = result.first;
                table[competitor - 1][team - 1] = result.second;
            }
        }
    }

    std::stringstream ans;
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            ans << table[i][j];
        }
        ans << '\n';
    }
    std::cout << ans.str();
}