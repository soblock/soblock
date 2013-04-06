package org.wavecraft.graphics.view;

public interface Projection {
	public void setProjectionMatrix();
	public void setViewPort();
	public int[] getViewPortDim();
}
