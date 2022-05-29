package com.singularitycoder.viewmodelstuff2.helpers

import java.security.SecureRandom

// https://www.raywenderlich.com/778533-encryption-tutorial-for-android-getting-started
// https://www.geeksforgeeks.org/how-to-encrypt-and-decrypt-text-in-android-using-cryptography/
// prefix “crypt” means “hidden” and suffix graphy means “writing”

// original message -> (encryption) -> encrypted message
// encrypted message -> (decryption) -> original message

// Encrypt and Decrypt a message using the Encoding and Decoding algorithm

// Encryption: It is the process of transforming a readable message into an unreadable one. To do so we use encoding algorithms.
// Decryption: It is the process of transforming data or information from an unreadable to readable form. To do so we use decoding algorithms.

// we will be converting the text into a binary number using an Encryption algorithm.

const val INIT_VECTOR = "11111111"

fun encode(originalText: String): String {
    val ini = INIT_VECTOR // create a string to add in the initial binary code for extra security
    var cu = 0
    val arr = IntArray(11111111) // create an array

    // iterate through the string
    for (i in 0 until originalText.length) {
        arr[i] = originalText[i].code // put the ascii value of each character in the array
        cu++
    }
    var encryptedText = ""
    val bin = IntArray(111) // create another array
    var idx = 0

    // run a loop of the size of string
    for (i1 in 0 until cu) {
        var temp = arr[i1] // get the ascii value at position i1 from the first array

        // run the second nested loop of same size and set 0 value in the second array
        for (j in 0 until cu) bin[j] = 0
        idx = 0

        // run a while for temp > 0
        while (temp > 0) {
            bin[idx++] = temp % 2 // store the temp module of 2 in the 2nd array
            temp /= 2
        }
        var dig = ""
        var temps: String

        // run a loop of size 7
        for (j in 0..6) {
            temps = Integer.toString(bin[j]) // convert the integer to string
            dig += temps // add the string using concatenation function
        }
        var revs = ""

        // reverse the string
        for (j in dig.length - 1 downTo 0) {
            val ca = dig[j]
            revs += ca.toString()
        }
        encryptedText += revs
    }
    encryptedText = ini + encryptedText // add the extra string to the binary code

    return encryptedText // return the encrypted code
}

fun decode(encryptedText: String): String {
    val ini = INIT_VECTOR // create the same initial string as in encode class
    var flag = true

    // run a loop of size 8
    for (i in 0..7) {
        if (ini[i] != encryptedText[i]) { // check if the initial value is same
            flag = false
            break
        }
    }
    var textValue = ""

    // reverse the encrypted code
    for (i in 8 until encryptedText.length) {
        val ch = encryptedText[i]
        textValue += ch.toString()
    }

    val arr = Array(11101) { IntArray(8) } // create a 2 dimensional array
    var ind1 = -1
    var ind2 = 0

    // run a loop of size of the encrypted code
    for (i in 0 until textValue.length) {
        // check if the position of the string if divisible by 7
        if (i % 7 == 0) {
            ind1++ // start the value in other column of the 2D array
            ind2 = 0
            val ch = textValue[i]
            arr[ind1][ind2] = ch - '0'
            ind2++
        } else {
            val ch = textValue[i] // otherwise store the value in the same column
            arr[ind1][ind2] = ch - '0'
            ind2++
        }
    }

    val num = IntArray(11111) // create an array
    var nind = 0
    var tem = 0
    var cu = 0

    // run a loop of size of the column
    for (i in 0..ind1) {
        cu = 0
        tem = 0
        // convert binary to decimal and add them from each column and store in the array
        for (j in 6 downTo 0) {
            val tem1 = Math.pow(2.0, cu.toDouble()).toInt()
            tem += arr[i][j] * tem1
            cu++
        }
        num[nind++] = tem
    }
    var originalText = ""
    var ch: Char
    // convert the decimal ascii number to its char value and add them to form a decrypted string using conception function
    for (i in 0 until nind) {
        ch = num[i].toChar()
        originalText += ch.toString()
    }
    println("Desc: text 11 - $originalText")

    // check if the encrypted code was generated for this algorithm
    return if (textValue.length % 7 == 0 && flag) {
        originalText // return the decrypted code
    } else {
        "Invalid Code" // otherwise return an invalid message
    }
}



