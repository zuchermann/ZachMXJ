package innards;

public class TKeyPair<T> implements KeyPair {

	Key perceptKey;
	TKey<T> dataKey;
	
	public TKeyPair(String str) {
		this.perceptKey = new Key(str + " percept key");
		this.dataKey = new TKey<T>(str + " key");
	}
	
	public TKeyPair(Key perceptKey, TKey<T> dataKey) {
		this.perceptKey = perceptKey;
		this.dataKey = dataKey;
	}
	
	public TKey<T> dataKey() {
		return dataKey;
	}

	public Key perceptKey() {
		return perceptKey;
	}

}
