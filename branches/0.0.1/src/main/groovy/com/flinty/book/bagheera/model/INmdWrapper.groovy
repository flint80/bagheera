package com.flinty.book.bagheera.model


import constructor.objects.channel.core.stream.ChannelDataList

/**
 * implementation is responsible for managing NMDParser library
 *
 */
interface INmdWrapper {

	String convert(ChannelDataList dataList, String coverUrl, ProgressCallback callback)


	static interface ProgressCallback{
		void onProgressChanged(int percent)
	}
}
