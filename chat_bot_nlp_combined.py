# -*- coding: utf-8 -*-
"""
Created on Wed Aug 24 17:10:50 2016

@author: Vipin
"""
import sys
import nltk

from nltk import word_tokenize, pos_tag

from nltk.corpus import stopwords

from nltk.stem.wordnet import WordNetLemmatizer

from collections import Counter
from math import sqrt,ceil
import re

print ("Hello World from Python")

  
def filter_tokens(input):
    # replace all apostrophe with blank
    tokens=re.sub('[^A-Za-z0-9]+'," ",input)    
    # tokenize the string    
    tokens=word_tokenize(tokens)
    # convert every character to lower case
    tokens=[word.lower() for word in tokens]
    # get the part of speech of each token
    tokens=pos_tag(tokens)
    # keep only nouns, adverb, adjective and verb tokens
    tokens=[word for word,pos in tokens if pos == 'NN' or pos=='NNS' or 
              pos=='NNP' or pos=='NNPS'or pos=='VBN' or pos=='VB' or pos=='VBD'
              or pos=='VBG' or pos=='VBP' or pos=='VBZ' or pos=='JJ' or pos=='RB']
    # remove stop words
    s=set(stopwords.words('english'))
    s.update(['please','help'])
    tokens = [word for word in tokens if word not in s]
    # lemmatize all tokens into its present form
    lmtzr = WordNetLemmatizer()
    tokens=[lmtzr.lemmatize(word,'v') for word in tokens]
    # remove all tokens having less than 2 characters
    tokens=[word for word in tokens if len(word) > 1]
    return tokens



def word2vec(word):
    # count the characters in word
    cw = Counter(word)
    # precomputes a set of the different characters
    sw = set(cw)
    # precomputes the "length" of the word vector
    lw = sqrt(sum(c*c for c in cw.values()))
    # return a tuple
    return cw, sw, lw

def cosdis(v1, v2):
    # which characters are common to the two words?
    common = v1[1].intersection(v2[1])
    # by definition of cosine distance we have
    return sum(v1[0][ch]*v2[0][ch] for ch in common)/v1[2]/v2[2]    
    
sent=["I've forgotten my PIN can you please help?",
      "how can I find my PIN?",
      "How can I change my PIN?",
      "I have forgotten my Barclaycard memorable word can you help?",
      "What happens if I lose my card?",
      "I have lost my card please help",
      "What happens to Contactless Mobile if I lose my Credit Card?",
      "If I damage one of my Barclaycard Cashback cards, does just that one get replaced, or both of them?",
      "I've forgotten my Barclaycard Secure Password what do I do?"]
      
      
ans=["You can instantly view your PIN online by logging into Barclaycard online servicing and selecting ‘View your PIN securely’ which you'll find under 'Accounts & services' in the top menu.",
     "You can log in to Barclaycard online servicing and select ‘View your PIN securely’ under 'Accounts & services' in the top menu.",
     "You can change your PIN at Barclays cash machine or the cash machine of many other banks in the UK.",
     "please select the 'Forgotten your memorable word' link on the login screen Step 2 of Barclaycard online servicing and follow the instructions to reset your memorable word.",
     "We'll close your card so that no more purchases can be made with it as soon as we are intimated about the loss.",
     "Give us a call on 08001510900 or, if you're abroad, on  +441604230230 as soon as you can",
     "If your card is lost or stolen please contact us immediately on 01604 230 230 or +44 1604 230 230 if you're abroad, and we will block it.",
     "If your Barclaycard Cashback card is damaged we’ll only replace the damaged card unless you request both cards to be replaced.",
     "select the 'Forgot your password, click here' link that appears on the transaction record. In the following page, type your log in name and then enter the registration information. You will then be able to select a new password."]      
      
      
# build keyword dictionary with the existing data        
key_words_list=[filter_tokens(test) for test in sent]

# input string
input_str = sys.argv[1] 

# extract all important tokens from the input
input_tokens=filter_tokens(input_str)

score_it_kw=list()    
max_score_it_kw=list()
score=list()
for key_words_list_index in range(len(key_words_list)):
    for key_words in key_words_list[key_words_list_index]:
        for input_tokens_word in input_tokens:
            va=word2vec(input_tokens_word)
            vb=word2vec(key_words) 
            score_it_kw.append(ceil(cosdis(va,vb)*100)/100)
        max_score_it_kw.append(max(score_it_kw))
        score_it_kw=list()
    score.append(sum(max_score_it_kw)/len(key_words_list[key_words_list_index]))
    max_score_it_kw=list()
              
if max(score)>=0.8 :    
    print(sent[score.index(max(score))])
    print(ans[score.index(max(score))])
else :
    print("Not found in the database")
