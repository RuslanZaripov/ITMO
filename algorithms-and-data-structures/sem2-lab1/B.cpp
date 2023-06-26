#include <iostream>
#include <vector>
#include <cmath>
using namespace std;
const int INF = 1e9;

vector<pair<long long, int>> tree;

pair<long long, int> newTop(pair<long long, int> a, pair<long long, int> b)
{
    if (a.first > b.first)
    {
        return b;
    }
    if (b.first > a.first)
    {
        return a;
    }
    return make_pair(a.first, a.second + b.second);
}

void set(int i, long long v, int x, int lx, int rx)
{
    if (rx - lx == 1)
        tree[x] = make_pair(v, 1);
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
        tree[x] = newTop(tree[2 * x + 1], tree[2 * x + 2]);
    }
}

pair<long long, int> tmin(int l, int r, int x, int lx, int rx)
{
    if (lx >= r || l >= rx)
        return make_pair(INF, 0);
    if (lx >= l && rx <= r)
        return tree[x];
    int m = (lx + rx) / 2;
    // cout << "left: " << tmin(l, r, 2 * x + 1, lx, m).first << " " << tmin(l, r, 2 * x + 1, lx, m).second << endl;
    // cout << "right: " << tmin(l, r, 2 * x + 2, m, rx).first << " " << tmin(l, r, 2 * x + 2, m, rx).second << endl;
    return newTop(tmin(l, r, 2 * x + 1, lx, m), tmin(l, r, 2 * x + 2, m, rx));
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
    tree.resize(2 * capacity - 1, make_pair(INF, 0));
    for (int i = 0; i < n; i++)
    {
        cin >> tree[capacity - 1 + i].first;
        tree[capacity - 1 + i].second = 1;
    }
    for (int i = capacity - 2; i >= 0; i--)
    {
        tree[i] = newTop(tree[2 * i + 1], tree[2 * i + 2]);
    }
    for (int i = 0; i < k; i++)
    {
        int op;
        cin >> op;
        if (op == 1)
        {
            int l;
            long long v;
            cin >> l >> v;
            set(l, v, 0, 0, capacity);
        }
        else if (op == 2)
        {
            int l, r;
            cin >> l >> r;
            pair<int, int> result = tmin(l, r, 0, 0, capacity);
            cout << result.first << " " << result.second << endl;
            // for (int i = 0; i < tree.size(); i++)
            // {
            //     cout << i << ": " << tree[i].first << " " << tree[i].second << endl;
            // }
        }
    }
}