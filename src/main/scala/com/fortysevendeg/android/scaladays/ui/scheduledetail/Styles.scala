/*
 * Copyright (C) 2015 47 Degrees, LLC http://47deg.com hello@47deg.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may
 *  not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.fortysevendeg.android.scaladays.ui.scheduledetail

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.TextUtils.TruncateAt
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.ViewGroup.LayoutParams._
import android.widget.ImageView.ScaleType
import android.widget._
import com.fortysevendeg.android.scaladays.R
import com.fortysevendeg.android.scaladays.model.{Location, Track, Speaker}
import com.fortysevendeg.android.scaladays.ui.commons.AsyncImageTweaks._
import com.fortysevendeg.android.scaladays.ui.commons.DateTimeTextViewTweaks._
import com.fortysevendeg.android.scaladays.ui.components.{IconTypes, PathMorphDrawable}
import com.fortysevendeg.macroid.extras.ImageViewTweaks._
import com.fortysevendeg.macroid.extras.LinearLayoutTweaks._
import com.fortysevendeg.macroid.extras.ResourcesExtras._
import com.fortysevendeg.macroid.extras.ScrollViewTweaks._
import com.fortysevendeg.macroid.extras.TextTweaks._
import com.fortysevendeg.macroid.extras.ViewGroupTweaks._
import com.fortysevendeg.macroid.extras.ViewTweaks._
import com.fortysevendeg.macroid.extras.DeviceVersion._
import macroid.FullDsl._
import macroid.{Ui, ActivityContextWrapper, ContextWrapper, Tweak}
import org.joda.time.DateTime

import scala.language.postfixOps

trait ActivityStyles {

  val rootStyle: Tweak[FrameLayout] = vMatchParent

  def scrollContentStyle(implicit context: ContextWrapper): Tweak[ScrollView] = {
    val paddingLeftRight = resGetDimensionPixelSize(R.dimen.padding_default)
    vMatchParent +
      vPadding(
        paddingLeftRight,
        resGetDimensionPixelSize(R.dimen.padding_schedule_detail_scroll_top),
        paddingLeftRight,
        0) +
      svRemoveVerticalScrollBar
  }

  val contentStyle: Tweak[LinearLayout] =
    vMatchParent +
      llVertical

  def descriptionContentLayoutStyle(implicit context: ContextWrapper): Tweak[LinearLayout] =
    vMatchWidth +
      llHorizontal +
      vPadding(0, resGetDimensionPixelSize(R.dimen.padding_schedule_detail_description_top), 0, 0)

  def speakersContentLayoutStyle(speakers: Seq[Speaker])(implicit context: ActivityContextWrapper): Tweak[LinearLayout] =
    vMatchWidth +
      (if (speakers.nonEmpty)
        llVertical +
          vPadding(0, resGetDimensionPixelSize(R.dimen.padding_schedule_detail_speaker_tb), 0, 0) +
          vgAddViews(speakers map (speaker => {
            val speakerLayout = new SpeakersDetailLayout(speaker)
            speakerLayout.content
          }))
      else vGone)

  val verticalLayoutStyle =
    llWrapWeightHorizontal +
      llVertical

  def toolBarTitleStyle(title: String)(implicit context: ContextWrapper): Tweak[TextView] = {
    val padding = resGetDimensionPixelSize(R.dimen.padding_default)
    vMatchHeight +
      tvText(title) +
      tvGravity(Gravity.BOTTOM) +
      tvColorResource(R.color.toolbar_title) +
      tvSizeResource(R.dimen.text_huge) +
      tvMaxLines(3) +
      tvEllipsize(TruncateAt.END) +
      vPadding(padding, 0, padding, padding)
  }

  def fabStyle(favorite: Boolean)(implicit context: ContextWrapper): Tweak[ImageView] = {
    val size = resGetDimensionPixelSize(R.dimen.size_schedule_detail_fab)
    lp[FrameLayout](size, size) +
      vMargin(resGetDimensionPixelSize(R.dimen.margin_schedule_detail_fab_left), resGetDimensionPixelSize(R.dimen.margin_schedule_detail_fab_top), 0, 0) +
      vBackground(if (favorite) R.drawable.fab_button_check else R.drawable.fab_button_no_check) +
      vPaddings(resGetDimensionPixelSize(R.dimen.padding_schedule_detail_fab)) +
      ivSrc(new PathMorphDrawable(
        defaultIcon = if (favorite) IconTypes.CHECK else IconTypes.ADD,
        defaultStroke = resGetDimensionPixelSize(R.dimen.stroke_schedule_detail_fab),
        defaultColor = Color.WHITE
      )) +
      (Lollipop ifSupportedThen vElevation(resGetDimension(R.dimen.padding_default_extra_small)) getOrElse Tweak.blank)
  }

  def iconCalendarStyle(implicit context: ContextWrapper): Tweak[ImageView] =
    lp[LinearLayout](resGetDimensionPixelSize(R.dimen.width_schedule_detail_calendar), WRAP_CONTENT) +
      ivSrc(R.drawable.detail_icon_schedule) +
      ivScaleType(ScaleType.FIT_START)

  def dateStyle(startTime: DateTime, timeZone: String)(implicit context: ContextWrapper): Tweak[TextView] =
    vWrapContent +
      tvSizeResource(R.dimen.text_big) +
      tvColor(context.application.getResources.getColor(R.color.primary)) +
      vPadding(0, 0, 0, resGetDimensionPixelSize(R.dimen.padding_default_extra_small)) +
      tvDateDateTime(startTime, timeZone)

  def roomStyle(location: Option[Location])(implicit context: ContextWrapper): Tweak[TextView] =
    vWrapContent +
      tvSizeResource(R.dimen.text_medium) +
      tvColorResource(R.color.text_schedule_detail_secondary) +
      vPadding(0, 0, 0, resGetDimensionPixelSize(R.dimen.padding_default_small)) +
      (location map (
        location =>
          vVisible +
            (if (location.mapUrl == "") {
              tvText(context.application.getString(R.string.roomName, location.name))
            } else {
              val content = new SpannableString(context.application.getString(R.string.roomName, location.name))
              content.setSpan(new UnderlineSpan(), 0, content.length(), 0)
              tvText(content) + On.click {
                Ui {
                  val intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(location.mapUrl))
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  context.application.startActivity(intent)
                }
              }
            })
        )
        getOrElse vGone)

  def trackStyle(track: Option[Track])(implicit context: ContextWrapper): Tweak[TextView] =
    vWrapContent +
      tvSizeResource(R.dimen.text_medium) +
      tvColorResource(R.color.text_schedule_detail_secondary) +
      vPadding(0, 0, 0, resGetDimensionPixelSize(R.dimen.padding_default_small)) +
      (track map (track => tvText(track.name) + vVisible) getOrElse vGone)

  def descriptionStyle(description: String)(implicit context: ContextWrapper): Tweak[TextView] =
    vWrapContent +
      tvSizeResource(R.dimen.text_medium) +
      tvColorResource(R.color.primary) +
      vPadding(0, 0, 0, resGetDimensionPixelSize(R.dimen.padding_default)) +
      tvText(description)

  def lineStyle(implicit context: ContextWrapper): Tweak[ImageView] =
    lp[LinearLayout](MATCH_PARENT, 1) +
      vBackgroundColorResource(R.color.list_line_default)

  def speakerTitleStyle(show: Boolean)(implicit context: ContextWrapper): Tweak[TextView] =
    vWrapContent +
      (if (show)
        tvSizeResource(R.dimen.text_small) +
          tvText(R.string.speakers) +
          vPadding(0, resGetDimensionPixelSize(R.dimen.padding_default_small), 0, 0) +
          tvColorResource(R.color.text_schedule_detail_secondary)
      else vGone)

}

trait SpeakersDetailStyles {

  def itemSpeakerContentStyle(implicit context: ContextWrapper): Tweak[LinearLayout] =
    vMatchWidth +
      llHorizontal +
      vPadding(0, resGetDimensionPixelSize(R.dimen.padding_default), 0, resGetDimensionPixelSize(R.dimen.padding_schedule_detail_speaker_tb)) +
      vBackground(R.drawable.background_list_default)

  def speakerAvatarStyle(picture: Option[String])(implicit context: ContextWrapper, activityContext: ActivityContextWrapper): Tweak[ImageView] = {
    val size = resGetDimensionPixelSize(R.dimen.size_avatar)
    lp[LinearLayout](size, size) +
      ivScaleType(ScaleType.CENTER_CROP) +
      vMargin(0, 0, resGetDimensionPixelSize(R.dimen.padding_default), 0) +
      (picture map {
        roundedImage(_, R.drawable.placeholder_circle, size.toInt, Some(R.drawable.placeholder_avatar_failed))
      } getOrElse ivSrc(R.drawable.placeholder_avatar_failed))
  }

  def speakerNameItemStyle(name: String)(implicit context: ContextWrapper): Tweak[TextView] =
    vWrapContent +
      tvSizeResource(R.dimen.text_big) +
      vPadding(0, 0, resGetDimensionPixelSize(R.dimen.padding_default_extra_small), 0) +
      tvColorResource(R.color.primary) +
      tvText(name)

  def speakerCompanyItemStyle(company: String)(implicit context: ContextWrapper): Tweak[TextView] =
    vWrapContent +
      tvSizeResource(R.dimen.text_medium) +
      vPadding(0, 0, resGetDimensionPixelSize(R.dimen.padding_default_extra_small), 0) +
      tvColorResource(R.color.text_schedule_detail_secondary) +
      tvText(company)

  def speakerTwitterItemStyle(twitter: Option[String])(implicit context: ContextWrapper): Tweak[TextView] =
    vWrapContent +
      tvSizeResource(R.dimen.text_small) +
      tvColorResource(R.color.text_twitter_default) +
      twitter.map(tvText(_) + vVisible).getOrElse(vGone)

  def speakerBioItemStyle(bio: String)(implicit context: ContextWrapper): Tweak[TextView] =
    vWrapContent +
      tvSizeResource(R.dimen.text_medium) +
      vPadding(0, 0, resGetDimensionPixelSize(R.dimen.padding_default_extra_small), 0) +
      tvColorResource(R.color.text_schedule_detail_secondary) +
      tvText(bio)

  val verticalLayoutStyle: Tweak[LinearLayout] =
    llWrapWeightHorizontal +
      llVertical

}
