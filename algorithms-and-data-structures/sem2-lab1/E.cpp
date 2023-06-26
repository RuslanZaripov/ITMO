#include <iostream>
#include <vector>
#include <cmath>
using namespace std;
const int INF = 1e9;

vector<long long> tree;

void set(int i, long long v, int x, int lx, int rx)
{
    if (rx - lx == 1)
        tree[x] = v;
    else
    {
        int m = (lx + rx) / 2;
        if (i < m)
        {
            set(i, v, 2 * x + 1, lx, m);
        }
        else
        {
            set(i, v, 2 * x + 2, m, rx);
        }
        tree[x] = max(tree[2 * x + 1], tree[2 * x + 2]);
    }
}

int find(long long v, int l, int x, int lx, int rx)
{
    if (rx < l || tree[x] < v)
        return -1;
    int left, right, mx, result = -1;
    bool answer;
    if (rx - lx == 1)
    {
        // cout << "lx: " << lx << endl;
        return lx;
    }
    left = 2 * x + 1;
    right = 2 * x + 2;
    mx = (lx + rx) / 2;
    answer = false;
    if (tree[left] >= v && l < mx)
    {
        // cout << "lx: " << lx << " l: " << l << " mx: " << mx << endl;
        result = find(v, l, left, lx, mx);
        // cout << "result  1:" << result << endl;
    }
    if (result == -1)
    {
        if (tree[right] != -INF)
            result = find(v, l, right, mx, rx);
        else 
            result = -1;
        // cout << "result  2:" << result << endl;
    }
    return result;
}

int main()
{
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);
    int n, k;
    cin >> n >> k;
    int capacity = 1;
    while (capacity < n)
    {
        capacity *= 2;
    }
    tree.resize(2 * capacity - 1, -INF);
    for (int i = 0; i < n; i++)
    {
        cin >> tree[capacity - 1 + i];
    }
    for (int i = capacity - 2; i >= 0; i--)
    {
        tree[i] = max(tree[2 * i + 1], tree[2 * i + 2]);
    }
    for (int i = 0; i < k; i++)
    {
        int op;
        cin >> op;
        if (op == 1)
        {
            int j;
            long long v;
            cin >> j >> v;
            set(j, v, 0, 0, capacity);
        }
        else if (op == 2)
        {
            int v, l;
            cin >> v >> l;
            cout << find(v, l, 0, 0, capacity) << endl;
        }
    }
}