from collections import defaultdict

import numpy as np
import tensorflow as tf


class MarkovChain:
    def __init__(self, m_text, m_n):
        self.m_chain = defaultdict(lambda: defaultdict(int))
        self.m_n = m_n
        self.train(m_text)

    def generate_text(self, text_length):
        state = np.random.choice(list(self.m_chain.keys()))

        m_text = state

        for i in range(text_length):
            if state not in self.m_chain:
                break

            probs = list(self.m_chain[state].values())
            next_char = np.random.choice(list(self.m_chain[state].keys()), p=probs)

            state = state[1:] + next_char
            m_text += next_char

        return m_text

    # m_text is the text to train on
    # m_n is the length of the state (number of characters)
    def train(self, m_text):
        for i in range(len(m_text) - self.m_n):
            state = m_text[i:i + self.m_n]
            next_char = m_text[i + self.m_n]
            self.m_chain[state][next_char] += 1

        alpha = 1

        def count_prob(m_next_char, m_total):
            return (self.m_chain[state][m_next_char] + alpha) / (m_total + alpha * len(self.m_chain[state]))

        for state in self.m_chain:
            total = sum(self.m_chain[state].values())
            for next_char in self.m_chain[state]:
                self.m_chain[state][next_char] = count_prob(next_char, total)


n = 10

text = open('shakespeare.txt', 'rb').read().decode(encoding='utf-8')

chain = MarkovChain(text, n)

markov_text = chain.generate_text(text_length=500)

print(markov_text)
