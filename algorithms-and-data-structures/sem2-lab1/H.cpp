#include <iostream>
#include <vector>
#include <cmath>
using namespace std;

vector<pair<long long, pair<long long, long long>>> tree;
vector<long long> tree_size;

void print()
{
    cout << "Tree:" << endl;
    for (int i = 1; i < tree.size(); i *= 2)
    {
        cout << "> ";
        for (int j = i; j < 2 * i; j++)
        {
            cout << "(" << tree[j - 1].first << "|" << tree[j - 1].second.first << "|" << tree[j - 1].second.second << ")";
        }
        cout << endl;
    }
}
//second.second = "=";
//second.first = "+";
pair<long long, pair<long long, long long>> upd(pair<long long, pair<long long, long long>> a, pair<long long, pair<long long, long long>> b, int left, int right)
{
    // cout << a.second.second << " " <<  b.second.second << endl;
    if (a.second.second != -1)
    {
        // cout << a.second.second << endl;
        if (b.second.first != 0)
        {
            return make_pair(tree_size[left] * a.second.second + b.first + tree_size[right] * b.second.first, make_pair(0, -1));
        }
        else if (b.second.second != -1)
        {
            // cout << a.second.second + (rx - mx) * b.second.second << endl;
            return make_pair(tree_size[left] * a.second.second + tree_size[right] * b.second.second, make_pair(0, -1));
        }
        else
        {
            // cout << "hello" << endl;
            // cout << mx << " " << lx << " " << a.second.second << " " << b.first << endl; 
            return make_pair(tree_size[left] * a.second.second + b.first, make_pair(0, -1));
        }
    }
    else if (a.second.first != 0)
    {
        if (b.second.first != 0)
        {
            // cout << (mx - lx) * (a.first + a.second.first) + (rx - mx) * (b.first + b.second.first) << endl;
            return make_pair(a.first + tree_size[left] * a.second.first + b.first + tree_size[right] * b.second.first, make_pair(0, -1));
        }
        else if (b.second.second != -1)
        {
            // cout << (mx - lx) * (a.first + a.second.first) + (rx - mx) * b.second.second << endl;
            return make_pair(a.first + tree_size[left] * a.second.first + tree_size[right] * b.second.second, make_pair(0, -1));
        }
        else
        {
            return make_pair(a.first + tree_size[left] * a.second.first + b.first, make_pair(0, -1));
        }
    }
    else
    {
        if (b.second.first != 0)
        {
            // cout << (mx - lx) * (a.first + a.second.first) + (rx - mx) * (b.first + b.second.first) << endl;
            return make_pair(a.first + b.first + tree_size[right] * b.second.first, make_pair(0, -1));
        }
        else if (b.second.second != -1)
        {
            // cout << (mx - lx) * (a.first + a.second.first) + (rx - mx) * b.second.second << endl;
            return make_pair(a.first + tree_size[right] * b.second.second, make_pair(0, -1));
        }
        else
        {
            return make_pair(a.first + b.first, make_pair(0, -1));
        }
    }
}
//second.second = "=";
//second.first = "+";
void fpropogate(int x)
{
    if (tree[x].second.first != 0)
    {
        if (tree[2 * x + 1].second.second != -1)
        {
            tree[2 * x + 1].second.second += tree[x].second.first;
        }
        else if (tree[2 * x + 1].second.first != 0)
        {
            tree[2 * x + 1].second.first += tree[x].second.first;
        }
        else
        {
            tree[2 * x + 1].second.first = tree[x].second.first;
        }

        if (tree[2 * x + 2].second.second != -1)
        {
            tree[2 * x + 2].second.second += tree[x].second.first;
        }
        else if (tree[2 * x + 2].second.first != 0)
        {
            tree[2 * x + 2].second.first += tree[x].second.first;
        }
        else
        {
            tree[2 * x + 2].second.first = tree[x].second.first;
        }

        tree[x].second.first = 0;
    }
    else if (tree[x].second.second != -1)
    {
        if (tree[2 * x + 1].second.second != -1)
        {
            tree[2 * x + 1].second.second = tree[x].second.second;
        }
        else if (tree[2 * x + 1].second.first != 0)
        {
            tree[2 * x + 1].second.second = tree[x].second.second;
            tree[2 * x + 1].second.first = 0;
        }
        else
        {
            tree[2 * x + 1].second.second = tree[x].second.second;
        }

        if (tree[2 * x + 2].second.second != -1)
        {
            tree[2 * x + 2].second.second = tree[x].second.second;
        }
        else if (tree[2 * x + 2].second.first != 0)
        {
            tree[2 * x + 2].second.second = tree[x].second.second;
            tree[2 * x + 2].second.first = 0;
        }
        else
        {
            tree[2 * x + 2].second.second = tree[x].second.second;
        }

        tree[x].second.second = -1;
    }
}
long long tsum(int l, int r, int x, int lx, int rx)
{
    int left = 2 * x + 1;
    int right = 2 * x + 2;
    if (lx >= r || l >= rx)
        return 0;
    if (lx >= l && rx <= r)
    {
        if (tree[x].second.second != -1)
        {
            return tree_size[x] * tree[x].second.second;
        }
        else if (tree[x].second.first != 0)
        {
            return tree[x].first + tree_size[x] * tree[x].second.first;
        }
        else
        {
            return tree[x].first;
        }
    }
    fpropogate(x);
    while (left != 0)
    {
        left = (left - 1) / 2;
        tree[left] = upd(tree[2 * left + 1], tree[2 * left + 2], 2 * left + 1, 2 * left + 2);
        // cout << tree[left].first << " " << tree[left].second.first << " " << tree[left].second.second << endl;
    }
    // cout << "========================================" << endl;
    int mx = (lx + rx) / 2;
    // int result = min(tmin(l, r, 2 * x + 1, lx, mx), tmin(l, r, 2 * x + 2, mx, rx));
    // cout << " result:  " << lx << ", " << rx << ": " << result << endl;
    return tsum(l, r, 2 * x + 1, lx, mx) + tsum(l, r, 2 * x + 2, mx, rx);
}
//second.second = "=";
//second.first = "+";
void upd_big_set(pair<long long, pair<long long, long long>> &a)
{
    a.second.first = 0;
}
void big_set(int l, int r, long long v, int x, int lx, int rx)
{
    int left = 2 * x + 1;
    int right = 2 * x + 2;
    if (lx >= r || l >= rx)
    {
        return;
    }
    if (lx >= l && rx <= r)
    {
        tree[x].second.second = v;
        upd_big_set(tree[x]);
        return;
    }
    fpropogate(x);
    int m = (lx + rx) / 2;
    big_set(l, r, v, left, lx, m);
    big_set(l, r, v, right, m, rx);
    while (left != 0)
    {
        left = (left - 1) / 2;
        tree[left] = upd(tree[2 * left + 1], tree[2 * left + 2], 2 * left + 1, 2 * left + 2);
        // print();
    }
}
//second.second = "=";
//second.first = "+";
void upd_big_add(pair<long long, pair<long long, long long>> &a)
{
    if (a.second.second != -1)
    {
        a.second.second += a.second.first;
        a.second.first = 0;
    }
}
void big_add(int l, int r, long long v, int x, int lx, int rx)
{
    int left = 2 * x + 1;
    int right = 2 * x + 2;
    if (lx >= r || l >= rx)
    {
        return;
    }
    if (lx >= l && rx <= r)
    {
        tree[x].second.first += v;
        upd_big_add(tree[x]);
        return;
    }
    fpropogate(x);
    int m = (lx + rx) / 2;
    big_add(l, r, v, left, lx, m);
    big_add(l, r, v, right, m, rx);
    while (left != 0)
    {
        left = (left - 1) / 2;
        tree[left] = upd(tree[2 * left + 1], tree[2 * left + 2], 2 * left + 1, 2 * left + 2);
    }
}
int main()
{
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);
    int n, m;
    cin >> n >> m;
    long long capacity = 1;
    while (capacity < n)
    {
        capacity *= 2;
    }
    tree.resize(2 * capacity - 1, make_pair(0, make_pair(0, -1)));
    tree_size.resize(2 * capacity - 1, 1);
    for (int i = 0; i < n; i++)
    {
        tree[capacity - 1 + i].first = 0;
    }
    for (int i = capacity - 2; i >= 0; i--)
    {
        tree[i].first = tree[2 * i + 1].first + tree[2 * i + 2].first;
        tree_size[i] = tree_size[2 * i + 1] + tree_size[2 * i + 2];
    }
    for (int i = 0; i < m; i++)
    {
        int op;
        cin >> op;
        if (op == 1)
        {
            int l, r;
            long long v;
            cin >> l >> r >> v;
            big_set(l, r, v, 0, 0, capacity);
            // print();
        }
        else if (op == 2)
        {
            int l, r;
            long long v;
            cin >> l >> r >> v;
            big_add(l, r, v, 0, 0, capacity);
            // print();
        }
        else if (op == 3)
        {
            int l, r;
            cin >> l >> r;
            cout << tsum(l, r, 0, 0, capacity) << endl;
            // print();
        }
    }
}