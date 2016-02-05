<%@ page contentType="text/html;charset=utf-8"%>

<div class="container">
	<form role="commentForm" method="post">
		<input type="hidden" id="creator" name="creator" value=${creator}> <input type="hidden" id="pageNo" name="pageNo"
			value=${pageNo}> <input type="hidden" id="num" name="num" value=${num}> <input type="hidden" id="user_name"
			name="user_name" value=${user_name}><input type="hidden" id="searchWord" name="searchWord" value=${searchWord}>
	</form>
	<input type="text" id="contents" name="contents" style="width: 100%;">
	<button class="btn btn-danger" id="btn_cmt_input" name="btn_cmt_input" style="width: 100%;">
		<span class="glyphicon glyphicon-comment" aria-hidden="true"></span>
	</button>
</div>
<script language="javascript">
	$( document ).ready( init );
	function init()
	{
		$( "#btn_cmt_input" ).click( function()
		{
			if ($( "#contents" ).val() == "")
				alert( "내용을 입력 해 주세요" );
			else
				fn_doCommentInsert();
		} );
	}
	function fn_doCommentInsert()
	{
		var formObj = $( "form[role='commentForm']" );
		var ctnt = $( '<input></input>' );
		ctnt.attr( "type", "hidden" );
		ctnt.attr( "name", "contents" );
		ctnt.attr( "id", "contents" );
		ctnt.attr( "value", $( "#contents" ).val() );
		formObj.append( ctnt );
		formObj.attr( "action", "/fis/board/voc/vocCommentWriter.moJson" );
		formObj.attr( "method", "post" );
		formObj.submit();
	}
	$( '#contents' ).keypress( function( event )
	{
		if (event.keyCode == 13)
		{
			if ($( "#contents" ).val() == "")
				alert( "내용을 입력 해 주세요" );
			else
				fn_doCommentInsert();
		}
	} );
</script>
