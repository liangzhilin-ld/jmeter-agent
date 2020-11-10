package com.autotest.jmeter.entity.theadgroup;

public class TheadGroupEntity {
	/**
	 * 线程组
	 */
	String name="线程组";
	/**
	 * Sampler发生错误时下一步处理
	 */
	String onSampleError="continue";
	/**
	 * 线程数
	 */
	String numThreads="1";
	/**
	 * 线程全部启动完成时间
	 */
	String rampUp="1";
	/**
	 * 线程循环次数
	 */
	String loopCount="1";
	/**
	 * 是否开启延时，默认不开启
	 */
	Boolean delayThredCreation=false;//默认不开启延时
	/**
	 * 是否开启调度器，默认不开启
	 */
	Boolean Scheduler=false;//默认不开启调度器
	/**
	 * 开启调度器后设置持续运行时间（秒）
	 */
	String duration="";
	/**
	 * 开启延时后设置延时时间（秒）
	 */
	String delay="";
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOnSampleError() {
		return onSampleError;
	}
	public void setOnSampleError(String onSampleError) {
		this.onSampleError = onSampleError;
	}
	public String getNumThreads() {
		return numThreads;
	}
	public void setNumThreads(String numThreads) {
		this.numThreads = numThreads;
	}
	public String getRampUp() {
		return rampUp;
	}
	public void setRampUp(String rampUp) {
		this.rampUp = rampUp;
	}
	public String getLoopCount() {
		return loopCount;
	}
	public void setLoopCount(String loopCount) {
		this.loopCount = loopCount;
	}
	public Boolean getDelayThredCreation() {
		return delayThredCreation;
	}
	public void setDelayThredCreation(Boolean delayThredCreation) {
		this.delayThredCreation = delayThredCreation;
	}
	public Boolean getScheduler() {
		return Scheduler;
	}
	public void setScheduler(Boolean scheduler) {
		Scheduler = scheduler;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getDelay() {
		return delay;
	}
	public void setDelay(String delay) {
		this.delay = delay;
	}

}
