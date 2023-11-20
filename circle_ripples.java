class circle_ripples{
	public static void main(String[] args){
		AnimationFrame af=new AnimationFrame();
	
		int num = 50;
		int spread = 0;
		//int x[] = {50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600};
		//int t[] = {0, -50, -100, -150, -200, -250, -300, -350, -400, -450, -500, -550};
		int x[] = new int[24];
		int t[] = new int[24];	

		for(int i = 0; i <= 23; i++){
			x[i] = num / 2;
			t[i] = (num - 50) * -1;
			num += 50;
		}

		while(true){
			for(int i = 0; i <= 23; i++){
				af.drawCircle(x[i] - i*10, 250, t[i] + spread, false);
			}
			af.paintFrame();
			spread += 15;
		}

	}

}