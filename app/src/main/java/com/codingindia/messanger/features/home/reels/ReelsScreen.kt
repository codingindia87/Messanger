package com.codingindia.messanger.features.home.reels

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelsScreen() {
    LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 20 })

    val videoUrls = listOf(
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-iad3-2.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQPKMNpiAfzufvvEvyJ9O_wMwdjAYYSoQuY5ub3WiE8IVPh7-vd3kdUrQGTPoku7O9qXuI-CHP4wuuO14M5NUZuMQ5Kl5e6cnVC4Nkw.mp4%3F_nc_cat%3D100%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-iad3-2.cdninstagram.com%26_nc_ohc%3DGW2uOLXrcCUQ7kNvwF-Vpmx%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6Mjc2Njc2NTUxMzY4NzA1NSwiYXNzZXRfYWdlX2RheXMiOjQ3LCJ2aV91c2VjYXNlX2lkIjoxMDA5OSwiZHVyYXRpb25fcyI6NiwidXJsZ2VuX3NvdXJjZSI6Ind3dyJ9%26ccb%3D17-1%26_nc_gid%3DTAf1wVHhjpH-2ZX7mKOyhw%26_nc_zt%3D28%26vs%3D7879a79bcfe7a124%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC83NDQ0MjZGMTlBN0E5M0FEODU2RkYxOEI0OTk1NzU4OV92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYR2lnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC8yMDQ0Nzc1ODA2MzAyOTk4XzU1NjM1NjczNzgxODM0MDkzMzAubXA0FQICyAESACgAGAAbAogHdXNlX29pbAExEnByb2dyZXNzaXZlX3JlY2lwZQExFQAAJp6A9rn5luoJFQIoAkMzLBdAGRBiTdLxqhgSZGFzaF9iYXNlbGluZV8xX3YxEQB1_gdl5p0BAA%26oh%3D00_Afs1djVhYS__n1QUfBP9_6p_bQemzeGimpzeexr3d5DvTQ%26oe%3D6994A15D",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-lax3-2.cdninstagram.com%2Fo1%2Fv%2Ft16%2Ff2%2Fm69%2FAQOT-eTHqIAdQRwxj12E2XrcWDigy44r51W1IAgPyirDWbqGCIJm8s5L2Yt3EqANjuPp_Jwr3yf5hxeDTOnmJCYZ.mp4%3Fstrext%3D1%26_nc_cat%3D108%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-lax3-2.cdninstagram.com%26_nc_ohc%3DSXUQVS-faCoQ7kNvwESQCNi%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc4NDkwNzE2NTA2ODE2MzgsImFzc2V0X2FnZV9kYXlzIjoyLCJ2aV91c2VjYXNlX2lkIjoxMDA5OSwiZHVyYXRpb25fcyI6MjAsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26_nc_gid%3Dl9mN6Uty26eTQ5SlvDqYaA%26_nc_zt%3D28%26vs%3D298db9fdd5c3b113%26_nc_vs%3DHBksFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HSGNlcGlXWU1rTVFMQnNEQUxCOW4yZ3hZY0FmYnNwVEFRQUYVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyLzUwNEMxQ0ZDNTQ3QTNGQjEwN0FFM0QwRjY5RDE5OEJEX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACbMzPn02ui0PxUCKAJDMywXQDTu2RaHKwIYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26oh%3D00_Afsg-pP78RHGROScxDwFG3MoTHhfs_Cy2dpfPCsNe18FrA%26oe%3D6998B9B0",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-iad3-2.cdninstagram.com%2Fo1%2Fv%2Ft16%2Ff2%2Fm69%2FAQPB9sW2YfeJAhIC8bQ7EKCGuWNwdcvH1IUVw0aivokCNPKtSnLg8XLqZjxb1-2vcfwMdvvBgCdT2yckgEDbFyHK.mp4%3Fstrext%3D1%26_nc_cat%3D111%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-iad3-2.cdninstagram.com%26_nc_ohc%3DsXguutwTufgQ7kNvwFMv8KD%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTg0MjM1NzkwMTAxODc3NTIsImFzc2V0X2FnZV9kYXlzIjo0LCJ2aV91c2VjYXNlX2lkIjoxMDA5OSwiZHVyYXRpb25fcyI6MTUsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26vs%3D3b58080465bcc8ef%26_nc_vs%3DHBksFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HTFJfanlYVV93YmdQU3dEQUJqUGlna1dSeWNVYnNwVEFRQUYVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyLzY0NEQ1QUQxQTgyMkY5RjZBMDEzODgzQzFCQTcyOUJBX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACbQh6amuIm6QRUCKAJDMywXQC8zMzMzMzMYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26_nc_gid%3Dcu3Hfxx-MFvdP0_vs4Qofg%26_nc_zt%3D28%26oh%3D00_Afv7VkxxZmshaWr2Er6IVVuTkDUSeOwOUznctouFN87o7w%26oe%3D69989FF9",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-iad3-2.cdninstagram.com%2Fo1%2Fv%2Ft16%2Ff2%2Fm69%2FAQN8cgWeug8N9DcLDgC9HT03a-QT-oQzkYsjVvehgLOuF_OANzlBYO8ACRTIsUi_U-vdaEJzQN3ARt0jTF4VS0C-.mp4%3Fstrext%3D1%26_nc_cat%3D111%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-iad3-2.cdninstagram.com%26_nc_ohc%3DFEQR1zfFne8Q7kNvwFw6-XK%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTg0MzA5OTMyMTYxMTk3MzUsImFzc2V0X2FnZV9kYXlzIjoyMywidmlfdXNlY2FzZV9pZCI6MTAwOTksImR1cmF0aW9uX3MiOjE5LCJ1cmxnZW5fc291cmNlIjoid3d3In0%253D%26ccb%3D17-1%26_nc_gid%3DsJHeYL5SSe_ke4xqZeBxww%26_nc_zt%3D28%26vs%3D2a89821a196afe6b%26_nc_vs%3DHBksFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HTFJ2NHlTRW9oQng1cFFKQUJpRUtZVC1RX2NFYnNwVEFRQUYVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyL0FDNDM1NTg3OTg5MURCMTNCODEwMUUyNkZENzVGNkFGX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACbujom4gLm9QRUCKAJDMywXQDPEGJN0vGoYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26oh%3D00_AfuITjsKNBcTZaZxDf1T-ZB-dWxg75R9MsPLNr2pEQ5qfQ%26oe%3D6998AB89",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-sjc3-1.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQOH2L_9GCOqcU9m_BOYn8rT0KhS7RWEQb9SeNPQfC9mTg7qJ3JpDPQ2x7UwonFqpPqx1PyLkiyhZUtvhnyVXT5upG4Emc1nDRxpIKw.mp4%3F_nc_cat%3D102%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-sjc3-1.cdninstagram.com%26_nc_ohc%3DBoZnxO0z2u8Q7kNvwG8jeYV%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTIxMjQ5MTYwNDEwMDEwMiwiYXNzZXRfYWdlX2RheXMiOjc2LCJ2aV91c2VjYXNlX2lkIjoxMDA5OSwiZHVyYXRpb25fcyI6MTYsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26_nc_gid%3Dp5eOlcHWpauycDUvkzjHsA%26_nc_zt%3D28%26vs%3D7dcbe780277435ef%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC9ERDRGMUY1QjNGMjY3RkFBNDFGMTgwMEU0RTIyMEY5Nl92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HRFA2VXlPb2tESTBJME1TQURHb1d4bXpZRFZQYnN0VEFRQUYVAgLIARIAKAAYABsCiAd1c2Vfb2lsATEScHJvZ3Jlc3NpdmVfcmVjaXBlATEVAAAmjJDYtaawpwQVAigCQzMsF0AwzMzMzMzNGBJkYXNoX2Jhc2VsaW5lXzFfdjERAHX-B2XmnQEA%26oh%3D00_AfuJLdM85fA-cTl4CsJ7wvQ0YGoBawc9tbutv5O6LpB1Ig%26oe%3D69949ACB",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-lax7-1.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQP1OFbC7Vsk4Obs6ygf3q77NuWx77ruLGO79svJBdpb9HZJmfYra_OgmmSGDhkPLA2ORXrl_ez5jQKdX7Mr6R3qNOveuuQt81ql-88.mp4%3F_nc_cat%3D100%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-lax7-1.cdninstagram.com%26_nc_ohc%3DYhDZ42CbqeMQ7kNvwH6hYdT%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6ODcyMTE2Nzg4NjAwMzAwLCJhc3NldF9hZ2VfZGF5cyI6NjEsInZpX3VzZWNhc2VfaWQiOjEwMDk5LCJkdXJhdGlvbl9zIjoxNSwidXJsZ2VuX3NvdXJjZSI6Ind3dyJ9%26ccb%3D17-1%26vs%3D73070ff290a5fe1b%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC83NjQ2MTk1REFENEU5NjY0RTMxNTUwMUQwMEZGRDI5RV92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYR2lnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC8xNTg4MDM3MjUyNjM1OTcxXzI1MjQzMTQ0MzA5NzE2ODg4NjgubXA0FQICyAESACgAGAAbAogHdXNlX29pbAExEnByb2dyZXNzaXZlX3JlY2lwZQExFQAAJtj3mLbwy4wDFQIoAkMzLBdALwAAAAAAABgSZGFzaF9iYXNlbGluZV8xX3YxEQB1_gdl5p0BAA%26_nc_gid%3DKt81-fw2oLoCMaYpCTaB5Q%26_nc_zt%3D28%26oh%3D00_AftQUgM4oMhJCXboePTrqjUoagWsy0AczrSoNwNfqSf2mw%26oe%3D6994D061",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-dfw5-2.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQM0XedXEOpwjbgcBmPKvOZ5waM-bZy0-L1UyIorPe5k9iuU0d2O1FZ-dptO0GxsMd1VcuZlk4FMgOD4MyMWWhtDZw_orMViSxctpOU.mp4%3F_nc_cat%3D104%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-dfw5-2.cdninstagram.com%26_nc_ohc%3DnfzmMznQbH0Q7kNvwF0raVw%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc4NDU1ODkzMTk2ODcwOTIsImFzc2V0X2FnZV9kYXlzIjoyLCJ2aV91c2VjYXNlX2lkIjoxMDgyNywiZHVyYXRpb25fcyI6MTAsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26vs%3D1793067f1f1f4133%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC8zNjQ1RjkxMDI4N0ZBOUU4QTg5Mzg2NEE4RUFDOUVBNV92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyLzhFNDM4NUI2REFENTg2QjI1M0I1NzY1OEJCMThGQUFFX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACbo7qjDgZ6zPxUCKAJDMywXQCQQ5WBBiTcYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZZapAQA%26_nc_gid%3DVeIrx8mKrxYWJ_iyWFZ9Bw%26_nc_zt%3D28%26oh%3D00_Afs3voIkr2O51fk9OfD9ElJl6H88CC3GFeFzrS4ugFGlLg%26oe%3D69949C55",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-lax7-1.cdninstagram.com%2Fo1%2Fv%2Ft16%2Ff2%2Fm69%2FAQM8q8I2FpwmPb9xTkbgDmQgXHp4_J0DmeCB98UPVLIPREXZYpm587E4EIwJ6UehpRpL3u3x_c5LLoJk53iTB_ho.mp4%3Fstrext%3D1%26_nc_cat%3D101%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-lax7-1.cdninstagram.com%26_nc_ohc%3DDVg4z3JRdzoQ7kNvwFlb12J%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc4ODkxMjk1NDk0MjYzMTgsImFzc2V0X2FnZV9kYXlzIjowLCJ2aV91c2VjYXNlX2lkIjoxMDA5OSwiZHVyYXRpb25fcyI6MTYsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26vs%3D851daa4c972f16f%26_nc_vs%3DHBksFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HS2JPdkNVNUdQUzNUVE1QQUtjZ0poN2VTaTltYnNwVEFRQUYVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyL0M4NEQ1QzcxNzNCQjczQUFDQjU4M0Y4NkI4QjNDM0JGX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACacupPCsYTHPxUCKAJDMywXQDC3S8an754YEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26_nc_gid%3Dw8XFkzTljj88NoT0R4FIeQ%26_nc_zt%3D28%26oh%3D00_AftTZuv982Og6UVvkGdY1HSrK-T8fnHCU8qtuyAd95m04w%26oe%3D69989C37",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-sjc3-1.cdninstagram.com%2Fo1%2Fv%2Ft16%2Ff2%2Fm69%2FAQNgR4ubyym7zSCqCm7kg8UjCU9xFdHvf5sAbspXUF4wMHrEUkLcbKEmOLF_L-yKjZzXudLq2FLlIURemRWb3P7Q.mp4%3Fstrext%3D1%26_nc_cat%3D110%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-sjc3-1.cdninstagram.com%26_nc_ohc%3DcyBa-rh77PsQ7kNvwHYr9w4%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc5MDE2MjM0MjQzNjQxODYsImFzc2V0X2FnZV9kYXlzIjo2LCJ2aV91c2VjYXNlX2lkIjoxMDA5OSwiZHVyYXRpb25fcyI6MjEsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26_nc_gid%3D-r5CZuFeVEj79Kif1D5LSQ%26_nc_zt%3D28%26vs%3D8ed66e37006da3b1%26_nc_vs%3DHBksFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HQTVocFNYZGx4cEkwT0lFQUsxOVVRZjNhNTA3YnNwVEFRQUYVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyL0E4NEUzQTc3QTZCMDhDODdFNkJDRkY1OTQyNUJCREIwX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACa0uuWR0NvMPxUCKAJDMywXQDUQ5WBBiTcYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26oh%3D00_Afsg-B_Hbo3Ouhnr6X3uEOkR_u2jjWIHz1p3nNTvdP7FBg%26oe%3D6998BAAE",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-lax3-2.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQPdmA1fbwdRGPM13QjtDrSVSzTnWYrAjje0quMelihweu5xZwn53ERz9o_EnzpKiQFWtJMDybtSJXb3ybUZdesS9-AdiZdw2DomO0g.mp4%3F_nc_cat%3D107%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-lax3-2.cdninstagram.com%26_nc_ohc%3DlQX8OSzrNU8Q7kNvwH9pc-P%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc4NjYyMzI3OTM1NjAwMjMsImFzc2V0X2FnZV9kYXlzIjoxNSwidmlfdXNlY2FzZV9pZCI6MTAwOTksImR1cmF0aW9uX3MiOjE1LCJ1cmxnZW5fc291cmNlIjoid3d3In0%253D%26ccb%3D17-1%26vs%3D530cc87670033808%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC81ODQ4QTBFNzhBNTc0RDUzMTg1QTEzNTIxMkVERkJBRV92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyLzNDNDY5OEE2NURCOEE2MDBFOUNGRURDRTdFNERENEIxX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACauz5Oyz8-8PxUCKAJDMywXQC-qfvnbItEYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26_nc_gid%3DXORgDeOQMz8y24D3jEOKAg%26_nc_zt%3D28%26oh%3D00_Afu7tSX6cd4v8i_RDkA-dJKe5CraqYT9IWUndXI-Ftt6iw%26oe%3D6994B864",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-lga3-1.cdninstagram.com%2Fo1%2Fv%2Ft16%2Ff2%2Fm69%2FAQNrRDJr1rzD-ldDUw2DURoBig0dXNcspeYr8uZWot6vNxxPMsBVVt-EgiIhWgPHRKgX1TAFCDGUdSuJS1WoKTgK.mp4%3Fstrext%3D1%26_nc_cat%3D108%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-lga3-1.cdninstagram.com%26_nc_ohc%3DItFpb2KqOX4Q7kNvwHejbX0%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MjA4MzQ4MTUwMjQxMDcwMywiYXNzZXRfYWdlX2RheXMiOjUxLCJ2aV91c2VjYXNlX2lkIjoxMDA5OSwiZHVyYXRpb25fcyI6MzIsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26vs%3D5f501dde45fbcfdf%26_nc_vs%3DHBksFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HQmZrRlNTQkU5c1B4UVFEQUY2aVlaa0prNGRwYnNwVEFRQUYVAALIARIAFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HQ1NLTnlSbWkwcXZkM1FGQUFGeHdCOEMxaUpXYnN0VEFRQUYVAgLIARIAKAAYABsCiAd1c2Vfb2lsATEScHJvZ3Jlc3NpdmVfcmVjaXBlATEVAAAmnp_H7cq6swcVAigCQzMsF0BAWZmZmZmaGBJkYXNoX2Jhc2VsaW5lXzFfdjERAHX-B2XmnQEA%26_nc_gid%3DOYob6J_KX6Y6zDMFTauQdg%26_nc_zt%3D28%26oh%3D00_AfswhzNErDcXbA-Ss3mtoV9ilJ2ZfWrPjpsakMm7eGWBUw%26oe%3D6998944C",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-dfw6-1.cdninstagram.com%2Fo1%2Fv%2Ft16%2Ff2%2Fm69%2FAQP84k0Lgu91vyJg0wl641tyAztoQX5SC-NqTf2W-0LCph5c3G1bPF9QPouh3rjQzUEO_Cz6_VPayikcHXrs47a6.mp4%3Fstrext%3D1%26_nc_cat%3D103%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-dfw6-1.cdninstagram.com%26_nc_ohc%3DxITT0BbBJYcQ7kNvwHNRuOO%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzE2LmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc4NjM0OTg2NDQ1OTIwNDgsImFzc2V0X2FnZV9kYXlzIjowLCJ2aV91c2VjYXNlX2lkIjoxMDA5OSwiZHVyYXRpb25fcyI6MTYsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26vs%3Dc4125860123e6f31%26_nc_vs%3DHBksFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HRlBFQVNhbWI1d3hYSUVIQUU5QkE0VXdtUnhOYnNwVEFRQUYVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyL0FFNEEzMDE1NjRBODAxNUQ4MEM3RUZCQTMyMTZFNzk0X2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACbg1sCyvLC7PxUCKAJDMywXQDBdsi0OVgQYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26_nc_gid%3DyX-TLBbiAEsYbKB8ZKBfMA%26_nc_zt%3D28%26oh%3D00_AfurZOGhopl_xzx0LswrS-nsi1_pmTb0GWkZ7LDZRR_oig%26oe%3D69989B1D",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-sjc6-1.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQP9IKSWs_8nHC8ZX07joiChTf_u7RzJBZfSbq2PmRfWzZGs8yh12rg4W2VyC9WhAV8iHDzbnYAtpMwUnMBgIOr8OcQlv4QpTOan2aE.mp4%3F_nc_cat%3D101%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-sjc6-1.cdninstagram.com%26_nc_ohc%3DbN9CuIvh4gsQ7kNvwGKI3qN%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc5NDM2NzMwNzcxMTE2MTYsImFzc2V0X2FnZV9kYXlzIjoxMSwidmlfdXNlY2FzZV9pZCI6MTAwOTksImR1cmF0aW9uX3MiOjE1LCJ1cmxnZW5fc291cmNlIjoid3d3In0%253D%26ccb%3D17-1%26vs%3D23714eae12c1db4a%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC8wMzRFNzJGQzI2QUQ3M0U3MjgyNTA3MkVEMTg2NzFCOF92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyL0VENDk3MDc1OTk5MTlDMDQ0NEY4RTYyMEY1M0Y3NjlFX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACaA7dC7nuvfPxUCKAJDMywXQC4AAAAAAAAYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26_nc_gid%3DtLj0rNDypupWRjAgjgL3jw%26_nc_zt%3D28%26oh%3D00_AfuNLpGQJYGb0lO-n5n-8ZT-e2AHqtY8zie7631_8KD0WQ%26oe%3D6994C9D2",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-lga3-1.cdninstagram.com%2Fo1%2Fv%2Ft16%2Ff2%2Fm69%2FAQM5q9bSScigPBYhPGCafB63hUyLtiwmUojNlFBTRZOPLG1uOGQuPHRykPLZYI8uUcDLsSe5BpzSABjudJy__yOQ.mp4%3Fstrext%3D1%26_nc_cat%3D108%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-lga3-1.cdninstagram.com%26_nc_ohc%3DRNfVJjG91qsQ7kNvwFxP5pL%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc4NTQxNjMzNDY2MTEzMzgsImFzc2V0X2FnZV9kYXlzIjoyNSwidmlfdXNlY2FzZV9pZCI6MTAwOTksImR1cmF0aW9uX3MiOjIzLCJ1cmxnZW5fc291cmNlIjoid3d3In0%253D%26ccb%3D17-1%26vs%3D10bf97b8231a03f3%26_nc_vs%3DHBksFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HR2IzekNSX2RXMlVSS2dKQVA3VFVTQ0E2NVF3YnNwVEFRQUYVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyLzI4NEJDRkVCMkIwQTdEMzRDQzZCQzIyOUVGRTJEMTg0X2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACaU8sqAi5G3PxUCKAJDMywXQDfEGJN0vGoYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26_nc_gid%3D2VoROVYEFOzsxdFMMfHgOQ%26_nc_zt%3D28%26oh%3D00_Afv_aC1V3rER85YTBuP92_w67jj311vjs2WI1dDFSF-x7A%26oe%3D6998B9A8",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-sjc3-1.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQOFGkxOIZFcMyCqm6SKyJvI4tPO5juj77m3SxI8NXNpH0ap3RJbixBHVBtbmEnyz2kjqauFeXTaggEcdqQeo3G7KqGi6TlsPv6w7N0.mp4%3F_nc_cat%3D105%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-sjc3-1.cdninstagram.com%26_nc_ohc%3DCxjcTvYzXZcQ7kNvwGBC2w9%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc4NzE2MTU3NTY1MzUxMTMsImFzc2V0X2FnZV9kYXlzIjoxMiwidmlfdXNlY2FzZV9pZCI6MTAwOTksImR1cmF0aW9uX3MiOjE4LCJ1cmxnZW5fc291cmNlIjoid3d3In0%253D%26ccb%3D17-1%26vs%3D4dc65fb33c16b94b%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC9CMDRGMkYwQjhDNkRGRjYzRTM0MjRFMkRBNTQ5NTM4NF92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyL0I2NDY5NkVCMEZEM0ZBRTdBNkQ4MjM0RjlERDI5NTgyX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACaSldjL-Yi_PxUCKAJDMywXQDKIcrAgxJwYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26_nc_gid%3D4N2MdZB0KVnfNOik4DvhDw%26_nc_zt%3D28%26oh%3D00_Afv5VIFSGx4Y0bEsvRnaS8OzJf1R4-eFhD1y-uyWeCG3qg%26oe%3D6994C7E2",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-iad3-1.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQPJsbnws9Uu3PhPjgKz6WHtc-oB7ZwmWxv01J6FqDXSrk5NMRUpOv3T1A7kkKssXrh_3N5dP_H7KoW1ufdeLe0kW5-Ahya5ajO_d3c.mp4%3F_nc_cat%3D107%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-iad3-1.cdninstagram.com%26_nc_ohc%3D4ucB31Y16ukQ7kNvwE6-l-q%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc5Mzk4MjEwMjAxMTkwNzEsImFzc2V0X2FnZV9kYXlzIjo3LCJ2aV91c2VjYXNlX2lkIjoxMDgyNywiZHVyYXRpb25fcyI6MTQsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26vs%3Db2cd6e0121fe78ce%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC80NjRGRjE2RDE2RDNGNUM0QkEwNTI1QkMxMjdDRkM4M192aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyLzkyNDAzOEI4NUE5Q0IxRTRGMDQ0Njk4RTBGMzM3OEJFX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACa-gOizgovePxUCKAJDMywXQCyIMSbpeNUYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZZapAQA%26_nc_gid%3Dv-ggnPJ0W5M_DO7O7SFNrw%26_nc_zt%3D28%26oh%3D00_AfuqMViiUCDtL7zyyQOKErh0FQ1fuXUImyj87L9b6QQ7Vg%26oe%3D6994D06C",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-lax3-2.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQOfczMZX6yAQ8Xokd1mNa_g4IFIjURFogB7JEPAtYn2xrJdNzuthoclU6WwZCZDfZfYwbmzH5w4DRdw-Al0fx0QdmuMWkVbSMTMXlg.mp4%3F_nc_cat%3D107%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-lax3-2.cdninstagram.com%26_nc_ohc%3D6McLWWm2vsYQ7kNvwGOVSH0%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc4ODcxODYyMDY0Mjg0MjgsImFzc2V0X2FnZV9kYXlzIjoxMSwidmlfdXNlY2FzZV9pZCI6MTAwOTksImR1cmF0aW9uX3MiOjMxLCJ1cmxnZW5fc291cmNlIjoid3d3In0%253D%26ccb%3D17-1%26vs%3Dfc90b96a777c364%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC8yMDQ3NjcwMzZDOThGQzAyNjE4RDhDNjkzRDAzRTA4Q192aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyL0Y3NDRGNEZFQ0IzQjE1RDZDQ0U5NTNDRjhDMzlFMkExX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACaYtOy9opPGPxUCKAJDMywXQD_mZmZmZmYYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26_nc_gid%3DHIrvsyyK7AwjDm03jy2zzQ%26_nc_zt%3D28%26oh%3D00_AftjXg9wf1y1HYiVD7OIKAuOXo4KCm9FuTp87F5CaAPvCw%26oe%3D6994C894",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-lax3-1.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQMCj5FHuJ6algyhiMdEpT42Wf1RBQeF7A-8gze-kMfVkUDWdcNovqJJ-VZJDMjCU0WmBvNkZeGAYRECTTGWklbgMm3v3zSRPPKqBxU.mp4%3F_nc_cat%3D108%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-lax3-1.cdninstagram.com%26_nc_ohc%3DHsEByWsbFVMQ7kNvwEAutlL%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc5MzkwNDQxNDMxMTgzNDUsImFzc2V0X2FnZV9kYXlzIjo4LCJ2aV91c2VjYXNlX2lkIjoxMDgyNywiZHVyYXRpb25fcyI6MjQsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26vs%3Dc6dbf49fcdc33fcb%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC8wODQ1NDI5NTZGNzBDNzYzQ0U5Rjg5RDFCQ0Y4NDZCOF92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyLzMxNDA3RjIwRkU3MEM4MTQyNzAxNEI1NTNBQzk3Nzk1X2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACaS4JWc5t3dPxUCKAJDMywXQDiZmZmZmZoYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZZapAQA%26_nc_gid%3DynDI8Vo-0kq1wqv83NKmiw%26_nc_zt%3D28%26oh%3D00_Aft36FK8FgIOb1JQorgWLkfRd_aR8phnciZaHaKfTUl3gA%26oe%3D6994C695",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-dfw5-2.cdninstagram.com%2Fo1%2Fv%2Ft2%2Ff2%2Fm86%2FAQMh2ULHFakylpPHsOLTWJrxV3nDCKhk9AWu3mzBCzc5QHHOqgBN6sp-ONjpH9tDFrRy5Z_4mFSYHkz50hFOVZuBG9IcC6gfrdU3olo.mp4%3F_nc_cat%3D100%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-dfw5-2.cdninstagram.com%26_nc_ohc%3Dwx9ovlSadi0Q7kNvwGtBiXp%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc5Mzg4NjYxODYxMTcxMTEsImFzc2V0X2FnZV9kYXlzIjoxOSwidmlfdXNlY2FzZV9pZCI6MTAwOTksImR1cmF0aW9uX3MiOjEzLCJ3YXRjaF90aW1lX3MiOjE1MDcxNTkyLCJ1cmxnZW5fc291cmNlIjoid3d3In0%253D%26ccb%3D17-1%26vs%3D1a9496fbd6e9c62a%26_nc_vs%3DHBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC8xMjRCRDI4QjY3RjU1NDkzOENGREIyNThGOEFFOUE4OF92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyL0Q3NEI3MkNEMjZCMEI3Rjk3M0MxNTcwMzgwRjNGRDgzX2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACbu_5KruNPdPxUCKAJDMywXQCpEGJN0vGoYEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26_nc_gid%3Dsx1jFq8Bj1_CEd6DZsvoBg%26_nc_zt%3D28%26oh%3D00_AftbXPFdT9KBMoLs_ryhyx31J1y20Itj5cy3iJn-u4bhOw%26oe%3D6994D779",
        "https://dl.fastvideosave.net/?url=https%3A%2F%2Fscontent-sea5-1.cdninstagram.com%2Fo1%2Fv%2Ft16%2Ff2%2Fm69%2FAQOb7ByI-9rUfjut8lMZPxT9VMo1q31SdtA8npD5VYOVRyjtal_HY7cnfmDGVz2J5jpxXyrd6MlZls7mkC5K7nTf.mp4%3Fstrext%3D1%26_nc_cat%3D111%26_nc_sid%3D5e9851%26_nc_ht%3Dscontent-sea5-1.cdninstagram.com%26_nc_ohc%3DeSMLgBgLInoQ7kNvwGkTP7_%26efg%3DeyJ2ZW5jb2RlX3RhZyI6Inhwdl9wcm9ncmVzc2l2ZS5JTlNUQUdSQU0uQ0xJUFMuQzMuNzIwLmRhc2hfYmFzZWxpbmVfMV92MSIsInhwdl9hc3NldF9pZCI6MTc5NzgwOTY2ODc5ODYyNDUsImFzc2V0X2FnZV9kYXlzIjowLCJ2aV91c2VjYXNlX2lkIjoxMDA5OSwiZHVyYXRpb25fcyI6MTQsInVybGdlbl9zb3VyY2UiOiJ3d3cifQ%253D%253D%26ccb%3D17-1%26vs%3D46b8d69a53f8d394%26_nc_vs%3DHBksFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HSHdMenlVQVpab1RCUVFQQUxRQkhuWlBOY3dBYnNwVEFRQUYVAALIARIAFQIYUWlnX3hwdl9wbGFjZW1lbnRfcGVybWFuZW50X3YyLzBBNDZEQUY3MUU3MUUwOTQxNDFGOTlFQTU1NjQ2NkE4X2F1ZGlvX2Rhc2hpbml0Lm1wNBUCAsgBEgAoABgAGwKIB3VzZV9vaWwBMRJwcm9ncmVzc2l2ZV9yZWNpcGUBMRUAACaK2Zmt-r7vPxUCKAJDMywXQC13S8an754YEmRhc2hfYmFzZWxpbmVfMV92MREAdf4HZeadAQA%26_nc_gid%3DarllC-t758uPGRiMlz8SAw%26_nc_zt%3D28%26oh%3D00_AfsBjsT4whK7O9r-ERBC9vrygEMKGAWk1zGsDAS7cQB70w%26oe%3D6998A101"
    )

    VerticalPager(
        state = pagerState, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 1
    ) { pageIndex ->

        // हर पेज एक अलग रील है
        Box(modifier = Modifier.fillMaxSize()) {

            // --- LAYER 1: Video Placeholder ---
            // यहाँ आप ExoPlayer (Media3) का उपयोग करके असली वीडियो दिखा सकते हैं
            VideoPlayerItem(
                url = videoUrls[pageIndex], isVisible = pagerState.currentPage == pageIndex
            )

            // --- LAYER 2: Side Action Buttons (Right Side) ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ReelActionItem(icon = Icons.Default.Favorite, label = "24K")
                ReelActionItem(icon = Icons.Default.Send, label = "Share")
                ReelActionItem(icon = Icons.Default.MoreVert, label = "")
            }

            // --- LAYER 3: Video Description (Bottom Left) ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 40.dp, end = 80.dp)
            ) {
                Text(
                    text = "@Sachin_Vishwakarma",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Jetpack Compose में 'Real View' बनाना बहुत आसान है! 🚀 #Kotlin #Compose",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// छोटे बटन्स के लिए Helper Composable
@Composable
fun ReelActionItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(32.dp)
                .clickable { /* Action */ })
        if (label.isNotEmpty()) {
            Text(text = label, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerItem(url: String, isVisible: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current // 1. Lifecycle Owner प्राप्त करें

    var isBuffering by remember { mutableStateOf(true) }
    var playbackProgress by remember { mutableStateOf(0f) }

    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            prepare()
            repeatMode = Player.REPEAT_MODE_ONE
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                }
            })
        }
    }

    // 2. Lifecycle Observer: ऐप के background/foreground जाने को ट्रैक करने के लिए
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // जब ऐप बैकग्राउंड में जाए या फोन लॉक हो
                    exoPlayer.pause()
                }

                Lifecycle.Event.ON_RESUME -> {
                    // जब ऐप वापस खुले, सिर्फ तभी चलाएं अगर रील सामने (visible) हो
                    if (isVisible) {
                        exoPlayer.play()
                    }
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    // 3. Pager Visibility Control: जब यूज़र रील स्वाइप करे
    LaunchedEffect(isVisible) {
        if (isVisible) {
            exoPlayer.play()
            while (true) {
                if (exoPlayer.duration > 0) {
                    playbackProgress = exoPlayer.currentPosition.toFloat() / exoPlayer.duration
                }
                kotlinx.coroutines.delay(200)
            }
        } else {
            exoPlayer.pause()
            exoPlayer.seekTo(0)
            playbackProgress = 0f
        }
    }

    // --- UI Layout ---
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            }, modifier = Modifier.fillMaxSize()
        )

        if (isBuffering) {
            CircularProgressIndicator(color = Color.White.copy(alpha = 0.5f))
        }

        LinearProgressIndicator(
            progress = { playbackProgress },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp),
            color = Color.White,
            trackColor = Color.Transparent,
        )
    }
}