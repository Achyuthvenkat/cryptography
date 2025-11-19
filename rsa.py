def gen_keys():
    p,q,e = 61,53,17
    n = p*q
    phi = (p-1)*(q-1)
    d = pow(e, -1, phi)
    return (n,e), (n,d)

def encrypt(pub, msg):
    n,e = pub
    return [pow(ord(c), e, n) for c in msg]

def decrypt(priv, cipher):
    n,d = priv
    return "".join(chr(pow(c, d, n)) for c in cipher)

pub, priv = gen_keys()
cipher = encrypt(pub, 'achyuth venkat')
print("ciphertext:", cipher)
print("decrypted:", decrypt(priv, cipher))