# Notes on the algorithm

The current algorithm for prediction works as follows:

1. Given a title, we remove all words in the list low_info_words.txt
 in the resources folder.

2. Next, form all k-letter combinations (_in order_) and then call 
these phrases. 

3. For each word in the title, form the Soundex equivalent. This 
Soundex value is a 4 character string like (X123), where X is a
letter. Against this soundex value, store a structure as follows:
`struct bests { list<pair<int, string>> top[LENGTH] }`
which is a list of the top LENGTH-strings for this Soundex, and the
integer represents the relative weight (which will be the number
of times it has occurred or has been selected). This LENGTH
should be small (from 4-10).

4. Against each word, also store the number of times it has been
selected / occurs in titles. When we update this, (say after
ingestion or getting a response), also compute its Soundex value
and update the field accordingly.

5. **Encapsulate this update in a synchronized method, so that multiple
updates occur one after the other**.

6. When we receive a word to fuzzy correct, find its Soundex.
Try changing some of the numbers, and check the list. **Don't 
change the first character.**

All of the above takes care of fuzzy fixes. Next we take care of 
word completions. 

1. Given a query string, split it into words. Try to fuzzy correct
the remaining strings (if they are not a low_info_string) except
the last one. For the last one, try to word-complete.

2. For this, keep a trie of all words, at each node of which is
the top LENGTH' completions. Simply pick some of the top.

3. Each time you receive a selected completion, update the
frequencies of all the words in the trie.

Next we take care of suggesting more tags.

1. For this part, keep all phrases we formed out of the titles
(in order, as we formed them before) in yet another trie, which
will be a popularity trie as the one being used above for the
word-completion. But the difference is, this time, the edges will
be strings. Consequently the height of this trie will be very less
(atmost LENGTH).

2. When you receive a phrase, say x y, try to find a string in
the trie that comes after x y. Rate these the maximum. Then
find ones that come after y, rate these the second max. 
In general for a combination like [i1, i2, .., ik], rate them in
order of sum of i1 + i2 .. + ik, so that matching things later is
scored higher.

3. When the selected phrase is received, just update the score for
each sub-phrase.


----

The first structure we need to write is a Popularity Trie, which
takes a list of values, and forms a trie out-of these values.
It also supports a hit method, that increases the hit-count of a
given value. This would update the popularity values for the list.

This implementation should be mostly synchronized, for it will be
accessed from multiple threads.

We will be treating a String as
`String -> LinkedList<Character>`


