package com.heetsakaria.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;




import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

	SpriteBatch batch;

	Texture background;
	Texture gameOver;

	//bird variables
	Texture[] birds;
	float birdY = 0;
	float birdX = 0;
	float birdVelocity = 0;
	Circle birdCircle;

	//animation speed variables
	int flapState = 0;
	float accumulator = 0f;
	float speed =  0.08f;

	//tube variables
	Texture toptube;
	Texture bottomtube;
	float gap = 400;
	float maxTubeOffsetY;
	float tubeVelocity = 4;
	int noOfTubes = 4;
	float[] tubeX  = new float[noOfTubes];
	float[] tubeOffsetY = new float[noOfTubes];
	float distanceBetweenTubes;
	Rectangle topTubeRects[] = new Rectangle[noOfTubes];
	Rectangle bottomTubeRects[] = new Rectangle[noOfTubes];

	Random random;

	//game variables
	int gameState = 0;
	int score = 0;
	int scoringTube = 0;
	int highscore = 0;
	BitmapFont font;

	Preferences prefs;

	@Override
	public void create ()
	{
		batch = new SpriteBatch();

		prefs = Gdx.app.getPreferences("My Preferences");

		gameOver = new Texture("gameover.png");
		background = new Texture("bg.png");

		birds = new Texture[2];
		birds[0] = new Texture("bird.png");
		birds[1] = new Texture("bird2.png");

		bottomtube = new Texture("bottomtube.png");
		toptube = new Texture("toptube.png");

		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(10);

		birdCircle = new Circle();
		birdX = (float)(Gdx.graphics.getWidth() - birds[flapState].getWidth())/2;
		birdY = (float)(Gdx.graphics.getHeight() - birds[flapState].getHeight())/2;

		random = new Random();
		maxTubeOffsetY = 500;
		distanceBetweenTubes = (float)Gdx.graphics.getWidth() * 0.75f;

		for(int i=0; i<noOfTubes; i++)
		{
			topTubeRects[i] = new Rectangle();
			bottomTubeRects[i] = new Rectangle();
			tubeOffsetY[i] = (random.nextFloat() - 0.5f) * (2* maxTubeOffsetY);
			tubeX[i] = (float)(Gdx.graphics.getWidth() - toptube.getWidth())/2 + Gdx.graphics.getWidth() + i*distanceBetweenTubes;
		}
	}

	@Override
	public void render () {

		batch.begin();
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(),  Gdx.graphics.getHeight());
		batch.draw(birds[flapState], birdX,  birdY);
		birdCircle.set(Gdx.graphics.getWidth()/2, birdY + birds[flapState].getHeight()/2, birds[flapState].getHeight()/2);

		if(gameState == 0) //start Game
		{
			birdY = (float)(Gdx.graphics.getHeight() - birds[flapState].getHeight())/2;
			birdVelocity = 0;
			scoringTube = 0;
			score = 0;
			highscore = prefs.getInteger("highscore", 0);
			//Gdx.app.log("highscore",String.valueOf(highscore));
			for(int i=0; i<noOfTubes; i++)
			{
				tubeOffsetY[i] = (random.nextFloat() - 0.5f) * (2* maxTubeOffsetY);
				tubeX[i] = (float) (Gdx.graphics.getWidth() - toptube.getWidth()) / 2 + Gdx.graphics.getWidth() + i * distanceBetweenTubes;
			}

			if (Gdx.input.justTouched())
			{
				gameState = 1;
			}
		}

		else if(gameState == 1)//during the game
		{
			//Controlling the speed of animation
			accumulator += Gdx.graphics.getDeltaTime();
			if(accumulator > speed)
			{
				flapState ^= 1;
				accumulator -= speed;
			}

			if(Gdx.input.justTouched())
			{
				birdVelocity = -20;
			}

			if(birdY > 0)
			{
				birdVelocity++;
				birdY -= birdVelocity;
				if(birdY >= (Gdx.graphics.getHeight()- birds[flapState].getHeight()))
					birdY = Gdx.graphics.getHeight() - birds[flapState].getHeight();
			}
			else
			{
				gameState = 2; //Game Over
			}

			for(int i=0; i<noOfTubes; i++)
			{
				if(tubeX[i] < -bottomtube.getWidth()) //jump to the right
				{
					tubeOffsetY[i] = (random.nextFloat() - 0.5f) * (2* maxTubeOffsetY);
					tubeX[i] += noOfTubes*distanceBetweenTubes;
				}
				else
				{
					tubeX[i] -= tubeVelocity;
				}
				batch.draw(toptube, tubeX[i], (Gdx.graphics.getHeight() + gap)/2 + tubeOffsetY[i]);
				batch.draw(bottomtube, tubeX[i], (Gdx.graphics.getHeight() - gap)/2 - bottomtube.getHeight() + tubeOffsetY[i]);

				topTubeRects[i].set(tubeX[i],(Gdx.graphics.getHeight() + gap)/2 + tubeOffsetY[i], toptube.getWidth(), toptube.getHeight());
				bottomTubeRects[i].set(tubeX[i],(Gdx.graphics.getHeight() - gap)/2 - bottomtube.getHeight() + tubeOffsetY[i], bottomtube.getWidth(), bottomtube.getHeight());

				if(Intersector.overlaps(birdCircle, topTubeRects[i]) || Intersector.overlaps(birdCircle, bottomTubeRects[i]))//collision
				{
					gameState = 2;
				}
				else if(birdCircle.x > (topTubeRects[scoringTube].x + topTubeRects[scoringTube].width))
				{
					scoringTube++;
					scoringTube = scoringTube %noOfTubes;
					score++;
				}
			}

		}

		else if(gameState == 2)
		{
			if(score>highscore)
			{
				highscore = score;
				prefs.putInteger("highscore", highscore);
				prefs.flush();
			}
			for(int i=0; i<noOfTubes; i++)
			{
				batch.draw(toptube, tubeX[i], (Gdx.graphics.getHeight() + gap)/2 + tubeOffsetY[i]);
				batch.draw(bottomtube, tubeX[i], (Gdx.graphics.getHeight() - gap)/2 - bottomtube.getHeight() + tubeOffsetY[i]);
			}
			batch.draw(gameOver, (float)(Gdx.graphics.getWidth() - gameOver.getWidth())/2, (float)(Gdx.graphics.getHeight()-gameOver.getHeight())/2);
			font.draw(batch, "Highscore: "+highscore, birdX - 350, 300);
			if (Gdx.input.justTouched()) {
				gameState = 0;
				score = 0;
			}
		}

		font.draw(batch, "Score: "+score, birdX - 200, Gdx.graphics.getHeight() - 200);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		background.dispose();
		birds[0].dispose();
		birds[1].dispose();
	}
}
