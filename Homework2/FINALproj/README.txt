

Το αρχείο CheckerVisitor.java περιέχει την υλοποίηση του δεύτερου visit.

Το αρχείο Main.java καλεί τις μεθόδους visit πάνω σε πολλαπλά αρχεία java και εκτελούνται όλα εκτυπώνοντας
τα offsets των πεδίων τους εφόσον δεν εντοπιστεί σε κάποιο error και γίνει throw exception.

Το Makefile περιέχει τις εντολές για το build του project με make. Με το make clean διέγραφα από το φάκελό μου
οποιοδήποτε αρχείο δημιουργούν τα jar και jj κρατώντας μόνο τα δικά μου αρχεία και τα παραδείγματα.

Το Symbol Table δημιουργείται στον πρώτο visitor και δίνεται σαν όρισμα στον δεύτερο.

Ένα πρόγραμμα το οποίο παρσάρω χωρίς να είμαι σίγουρος αν πρέπει στη minijava είναι της εξής μορφής:

class Add {
	public static void main(String[] a){
		System.out.println(12 + 21);
	}
}

class A {
	int i;

	public A foo() {
		return this;
	}
}

class B extends A {
	int i;

	public B foo() {
		return this;
	}
}

Δηλαδή οι overiding μέθοδοι ναι μεν να έχουν διαφορετικό τύπο αλλά ο ένας να είναι subclass του άλλου.

Για να το δεχτεί, πάω στο CheckerVisitor.java και ξεσχολιάζω γραμμή 493 έως 524 και σχολιάζω 526. Αλλιώς το ανάποδο.

στο piazza σε άλλο σχόλιο λέει να μη δεχόμαστε τέτοιο πρόγραμμα, αλλά σύμφωνα με το σύνδεσμο που δόθηκε για το github
πρέπει να το δεχόμαστε όπως εδώ https://github.com/baziotis/minijava-testsuite/blob/master/mainClass.java.

Πάντως με τα σχόλια όπως τα περιέγραψα μπορεί να γίνει και το ένα και το άλλο.